package chess.search;

import chess.hash.RepetitionTracker;
import chess.hash.TranspositionTable;
import chess.model.Board;
import chess.model.GameStateDetector;
import chess.model.Move;
import chess.movegen.MoveGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Search {
    public static final int INFINITY = 1000000;
    public static final int MATE_SCORE = 100000;
    private static final int MAX_PLY = 64;

    private final Evaluator evaluator;
    private final RepetitionTracker repetitionTracker;
    private final TranspositionTable transpositionTable;

    private long nodes;
    private long stopTimeMs;
    private long timeLimitMs;
    private boolean timeExceeded;

    public Search() {
        this(new MaterialPstEvaluator(), new RepetitionTracker(), new TranspositionTable());
    }

    public Search(Evaluator evaluator) {
        this(evaluator, new RepetitionTracker(), new TranspositionTable());
    }

    public Search(Evaluator evaluator, RepetitionTracker repetitionTracker) {
        this(evaluator, repetitionTracker, new TranspositionTable());
    }

    public Search(Evaluator evaluator, RepetitionTracker repetitionTracker, TranspositionTable transpositionTable) {
        this.evaluator = evaluator;
        this.repetitionTracker = repetitionTracker;
        this.transpositionTable = transpositionTable != null ? transpositionTable : new TranspositionTable();
    }

    public TranspositionTable getTranspositionTable() {
        return transpositionTable;
    }

    public SearchResult search(Board board, int maxDepth) {
        return search(board, maxDepth, 0, 1);
    }

    public SearchResult search(Board board, int maxDepth, long timeLimitMs) {
        return search(board, maxDepth, timeLimitMs, 1);
    }

    public SearchResult search(Board board, int maxDepth, long timeLimitMs, int topN) {
        List<Move> legalMoves = MoveGenerator.generateLegalMoves(board);
        if (legalMoves.isEmpty()) {
            int score = board.isInCheck(board.getSideToMove()) ? -MATE_SCORE : 0;
            return new SearchResult(null, score, 0, 0, 0, Collections.emptyList());
        }

        if (legalMoves.size() == 1) {
            int score = evaluator.evaluate(board);
            Move singleMove = legalMoves.get(0);
            return new SearchResult(singleMove, score, 1, 1, 0, List.of(new RootMoveScore(singleMove, score)));
        }

        long startTime = System.currentTimeMillis();
        this.timeLimitMs = timeLimitMs;
        this.stopTimeMs = timeLimitMs > 0 ? startTime + timeLimitMs : Long.MAX_VALUE;
        this.timeExceeded = false;
        this.nodes = 0;

        transpositionTable.newSearch();

        Move bestMoveOverall = legalMoves.get(0);
        int bestScoreOverall = -INFINITY;
        int completedDepth = 0;
        List<RootMoveScore> lastCompletedRootScores = new ArrayList<>();

        for (int depth = 1; depth <= maxDepth; depth++) {
            List<RootMoveScore> currentRootScores = new ArrayList<>();
            Move currentBestMove = null;
            int currentBestScore = -INFINITY;

            TranspositionTable.Entry rootEntry = transpositionTable.probe(board.getZobristHash());
            Move preferredMove = (rootEntry != null && rootEntry.move != null) ? rootEntry.move : bestMoveOverall;

            if (preferredMove != null) {
                final Move pvMove = preferredMove;
                legalMoves.sort((m1, m2) -> {
                    if (m1.equals(pvMove)) return -1;
                    if (m2.equals(pvMove)) return 1;
                    return Integer.compare(MoveOrderer.scoreMove(m2), MoveOrderer.scoreMove(m1));
                });
            } else {
                MoveOrderer.orderMoves(legalMoves);
            }

            int alpha = -INFINITY;
            int beta = INFINITY;

            for (Move move : legalMoves) {
                board.makeMove(move);
                repetitionTracker.pushSearchPosition(board.getZobristHash());

                int score = -negamax(board, depth - 1, -beta, -alpha, 1);

                repetitionTracker.popSearchPosition();
                board.unmakeMove();

                if (timeExceeded) {
                    break;
                }

                currentRootScores.add(new RootMoveScore(move, score));
                if (score > currentBestScore) {
                    currentBestScore = score;
                    currentBestMove = move;
                }
                if (score > alpha) {
                    alpha = score;
                }
            }

            if (timeExceeded) {
                break;
            }

            currentRootScores.sort(null);
            lastCompletedRootScores = currentRootScores;
            bestMoveOverall = currentBestMove;
            bestScoreOverall = currentBestScore;
            completedDepth = depth;

            transpositionTable.store(board.getZobristHash(), completedDepth, bestScoreOverall, TranspositionTable.EXACT, bestMoveOverall, 0);

            if (bestScoreOverall >= MATE_SCORE - 100) {
                break;
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        int count = Math.min(topN, lastCompletedRootScores.size());
        List<RootMoveScore> returnedTopMoves = lastCompletedRootScores.subList(0, count);

        return new SearchResult(bestMoveOverall, bestScoreOverall, completedDepth, nodes, elapsed, returnedTopMoves);
    }

    private int negamax(Board board, int depth, int alpha, int beta, int ply) {
        if (isTimeUp()) {
            return 0;
        }

        if (ply > 0) {
            if (board.getHalfmoveClock() >= 100) {
                return 0;
            }
            if (GameStateDetector.isInsufficientMaterial(board)) {
                return 0;
            }
            if (repetitionTracker.isSearchDraw(board.getZobristHash())) {
                return 0;
            }
        }

        int alphaOrig = alpha;
        TranspositionTable.Entry entry = transpositionTable.probe(board.getZobristHash());
        if (entry != null && entry.depth >= depth && ply > 0) {
            int ttScore = entry.getScore(ply);
            if (entry.flag == TranspositionTable.EXACT) {
                return ttScore;
            } else if (entry.flag == TranspositionTable.LOWERBOUND) {
                if (ttScore >= beta) return ttScore;
                if (ttScore > alpha) alpha = ttScore;
            } else if (entry.flag == TranspositionTable.UPPERBOUND) {
                if (ttScore <= alpha) return ttScore;
                if (ttScore < beta) beta = ttScore;
            }
        }

        if (depth <= 0 || ply >= MAX_PLY) {
            return quiescence(board, alpha, beta, ply);
        }

        nodes++;

        List<Move> moves = MoveGenerator.generateLegalMoves(board);
        if (moves.isEmpty()) {
            if (board.isInCheck(board.getSideToMove())) {
                return -MATE_SCORE + ply;
            }
            return 0;
        }

        Move ttMove = (entry != null) ? entry.move : null;
        if (ttMove != null) {
            moves.sort((m1, m2) -> {
                if (m1.equals(ttMove)) return -1;
                if (m2.equals(ttMove)) return 1;
                return Integer.compare(MoveOrderer.scoreMove(m2), MoveOrderer.scoreMove(m1));
            });
        } else {
            MoveOrderer.orderMoves(moves);
        }

        int bestScore = -INFINITY;
        Move bestMoveThisNode = null;

        for (Move move : moves) {
            board.makeMove(move);
            repetitionTracker.pushSearchPosition(board.getZobristHash());

            int score = -negamax(board, depth - 1, -beta, -alpha, ply + 1);

            repetitionTracker.popSearchPosition();
            board.unmakeMove();

            if (timeExceeded) {
                return 0;
            }

            if (score > bestScore) {
                bestScore = score;
                bestMoveThisNode = move;
            }
            if (score > alpha) {
                alpha = score;
            }
            if (alpha >= beta) {
                break;
            }
        }

        if (!timeExceeded) {
            byte flag;
            if (bestScore <= alphaOrig) {
                flag = TranspositionTable.UPPERBOUND;
            } else if (bestScore >= beta) {
                flag = TranspositionTable.LOWERBOUND;
            } else {
                flag = TranspositionTable.EXACT;
            }
            transpositionTable.store(board.getZobristHash(), depth, bestScore, flag, bestMoveThisNode, ply);
        }

        return bestScore;
    }

    private int quiescence(Board board, int alpha, int beta, int ply) {
        if (isTimeUp()) {
            return 0;
        }

        nodes++;

        if (ply >= MAX_PLY - 1) {
            return evaluator.evaluate(board);
        }

        int standPat = evaluator.evaluate(board);
        if (standPat >= beta) {
            return beta;
        }
        if (standPat > alpha) {
            alpha = standPat;
        }

        List<Move> pseudoMoves = MoveGenerator.generatePseudoLegalMoves(board);
        List<Move> captures = new ArrayList<>(pseudoMoves.size());
        for (Move move : pseudoMoves) {
            if (move.isCapture() || move.isPromotion()) {
                captures.add(move);
            }
        }

        MoveOrderer.orderMoves(captures);

        for (Move move : captures) {
            board.makeMove(move);
            if (board.isInCheck(board.getSideToMove().opposite())) {
                board.unmakeMove();
                continue;
            }

            int score = -quiescence(board, -beta, -alpha, ply + 1);
            board.unmakeMove();

            if (timeExceeded) {
                return 0;
            }

            if (score >= beta) {
                return beta;
            }
            if (score > alpha) {
                alpha = score;
            }
        }

        return alpha;
    }

    private boolean isTimeUp() {
        if (timeLimitMs <= 0) {
            return false;
        }
        if ((nodes & 2047) == 0) {
            if (System.currentTimeMillis() >= stopTimeMs) {
                timeExceeded = true;
            }
        }
        return timeExceeded;
    }
}
