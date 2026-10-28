package chess.ui;

import chess.hash.RepetitionTracker;
import chess.io.Fen;
import chess.model.*;
import chess.movegen.MoveGenerator;
import chess.search.MaterialPstEvaluator;
import chess.search.RootMoveScore;
import chess.search.Search;
import chess.search.SearchResult;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

public final class ConsoleGame {
    private final Board board;
    private final RepetitionTracker repetitionTracker;
    private final Search search;
    private final MaterialPstEvaluator evaluator;
    private final Scanner scanner;
    private final PrintStream out;

    private PieceColor humanColor = PieceColor.WHITE;
    private int searchDepth = 5;
    private long searchTimeMs = 1500;

    public ConsoleGame() {
        this(System.in, System.out);
    }

    public ConsoleGame(InputStream in, PrintStream out) {
        this.board = Fen.parse(Fen.START_FEN);
        this.repetitionTracker = new RepetitionTracker();
        this.repetitionTracker.addGamePosition(board.getZobristHash());
        this.search = new Search(new MaterialPstEvaluator(), repetitionTracker);
        this.evaluator = new MaterialPstEvaluator();
        this.scanner = new Scanner(in);
        this.out = out;
    }

    public static void main(String[] args) {
        ConsoleGame game = new ConsoleGame();
        game.run();
    }

    public void setHumanColor(PieceColor humanColor) {
        this.humanColor = humanColor;
    }

    public void setSearchDepth(int searchDepth) {
        this.searchDepth = searchDepth;
    }

    public void setSearchTimeMs(long searchTimeMs) {
        this.searchTimeMs = searchTimeMs;
    }

    public Board getBoard() {
        return board;
    }

    public void run() {
        out.println("Java Chess Engine - Console Play");
        out.println("Type moves as 'e2e4' or 'e7e8q'. Commands: 'moves', 'eval', 'undo', 'fen', 'new', 'quit'.\n");

        while (true) {
            out.print(renderBoard(board));
            GameState state = GameStateDetector.getGameState(board, repetitionTracker);
            if (state != GameState.ACTIVE) {
                printGameOver(state);
                break;
            }

            if (board.isInCheck(board.getSideToMove())) {
                out.println("Check!");
            }

            if (humanColor == null || board.getSideToMove() == humanColor) {
                if (!processHumanTurn()) {
                    break;
                }
            } else {
                processEngineTurn();
            }
        }
    }

    public boolean processHumanTurn() {
        while (true) {
            out.print("[" + board.getSideToMove() + "] Your move: ");
            if (!scanner.hasNextLine()) {
                return false;
            }
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                return false;
            }
            if (input.equalsIgnoreCase("moves")) {
                printLegalMoves();
                continue;
            }
            if (input.equalsIgnoreCase("eval")) {
                int eval = evaluator.evaluateAbsolute(board);
                out.printf("Evaluation: %+d cp (%+.2f pawns)\n", eval, eval / 100.0);
                continue;
            }
            if (input.equalsIgnoreCase("undo")) {
                undoMove();
                if (humanColor != null && board.getSideToMove() != humanColor) {
                    undoMove();
                }
                out.print(renderBoard(board));
                continue;
            }
            if (input.equalsIgnoreCase("fen")) {
                out.println("FEN: " + Fen.export(board));
                continue;
            }
            if (input.equalsIgnoreCase("new")) {
                board.clear();
                Board fresh = Fen.parse(Fen.START_FEN);
                for (int i = 0; i < 64; i++) board.setPiece(i, fresh.getPiece(i));
                board.setSideToMove(fresh.getSideToMove());
                board.setWhiteCastleKingside(fresh.canWhiteCastleKingside());
                board.setWhiteCastleQueenside(fresh.canWhiteCastleQueenside());
                board.setBlackCastleKingside(fresh.canBlackCastleKingside());
                board.setBlackCastleQueenside(fresh.canBlackCastleQueenside());
                board.setEnPassantSquare(fresh.getEnPassantSquare());
                board.setHalfmoveClock(fresh.getHalfmoveClock());
                board.setFullmoveNumber(fresh.getFullmoveNumber());
                board.recomputeHash();
                repetitionTracker.clear();
                repetitionTracker.addGamePosition(board.getZobristHash());
                out.print(renderBoard(board));
                continue;
            }

            Move move = parseMove(input);
            if (move == null) {
                out.println("Invalid or illegal move. Type 'moves' to see legal options.");
                continue;
            }

            board.makeMove(move);
            repetitionTracker.addGamePosition(board.getZobristHash());
            return true;
        }
    }

    public void processEngineTurn() {
        out.println("Engine is calculating (depth " + searchDepth + ", " + searchTimeMs + " ms)...");
        SearchResult result = search.search(board, searchDepth, searchTimeMs, 3);
        Move bestMove = result.getBestMove();
        if (bestMove == null) {
            return;
        }

        out.printf("Engine played: %s (eval: %+d, depth: %d, nodes: %d, time: %d ms)\n",
                bestMove.toUci(), result.getScore(), result.getDepth(), result.getNodes(), result.getTimeMs());

        List<RootMoveScore> topMoves = result.getTopMoves();
        if (topMoves.size() > 1) {
            out.print("Top moves: ");
            for (RootMoveScore r : topMoves) {
                out.print(r + "  ");
            }
            out.println();
        }

        board.makeMove(bestMove);
        repetitionTracker.addGamePosition(board.getZobristHash());
    }

    private void undoMove() {
        board.unmakeMove();
        repetitionTracker.removeLastGamePosition();
    }

    public Move parseMove(String text) {
        String cleaned = text.trim().replace("-", "").replace(" ", "").toLowerCase();
        List<Move> legalMoves = MoveGenerator.generateLegalMoves(board);

        for (Move move : legalMoves) {
            if (move.toUci().equalsIgnoreCase(cleaned)) {
                return move;
            }
        }

        if (cleaned.length() == 4) {
            int from = Square.fromName(cleaned.substring(0, 2));
            int to = Square.fromName(cleaned.substring(2, 4));
            Move candidate = null;
            for (Move move : legalMoves) {
                if (move.getFrom() == from && move.getTo() == to) {
                    if (move.isPromotion()) {
                        if (move.getPromotionPiece().getType() == PieceType.QUEEN) {
                            return move;
                        }
                    } else {
                        candidate = move;
                    }
                }
            }
            return candidate;
        }

        return null;
    }

    private void printLegalMoves() {
        List<Move> legalMoves = MoveGenerator.generateLegalMoves(board);
        out.print("Legal moves (" + legalMoves.size() + "): ");
        for (Move m : legalMoves) {
            out.print(m.toUci() + " ");
        }
        out.println();
    }

    private void printGameOver(GameState state) {
        switch (state) {
            case CHECKMATE -> {
                PieceColor winner = board.getSideToMove().opposite();
                out.println("\nCheckmate! " + winner + " wins!");
            }
            case STALEMATE -> out.println("\nDraw by stalemate!");
            case DRAW_FIFTY_MOVES -> out.println("\nDraw by fifty-move rule!");
            case DRAW_INSUFFICIENT_MATERIAL -> out.println("\nDraw by insufficient material!");
            case DRAW_THREEFOLD_REPETITION -> out.println("\nDraw by threefold repetition!");
            default -> {}
        }
    }

    public static String renderBoard(Board board) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n  +---+---+---+---+---+---+---+---+\n");
        for (int rank = 7; rank >= 0; rank--) {
            sb.append(rank + 1).append(" |");
            for (int file = 0; file < 8; file++) {
                Piece piece = board.getPiece(file, rank);
                char c = piece == null ? '.' : piece.getSymbol();
                sb.append(' ').append(c).append(" |");
            }
            sb.append("\n  +---+---+---+---+---+---+---+---+\n");
        }
        sb.append("    a   b   c   d   e   f   g   h\n\n");
        return sb.toString();
    }
}
