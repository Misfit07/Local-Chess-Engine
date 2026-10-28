package chess.model;

import chess.hash.RepetitionTracker;
import chess.movegen.MoveGenerator;

import java.util.List;

public final class GameStateDetector {
    private GameStateDetector() {}

    public static boolean isCheckmate(Board board) {
        return board.isInCheck(board.getSideToMove()) && MoveGenerator.generateLegalMoves(board).isEmpty();
    }

    public static boolean isStalemate(Board board) {
        return !board.isInCheck(board.getSideToMove()) && MoveGenerator.generateLegalMoves(board).isEmpty();
    }

    public static boolean isFiftyMoveRule(Board board) {
        return board.getHalfmoveClock() >= 100;
    }

    public static boolean isInsufficientMaterial(Board board) {
        int whiteKnights = 0, blackKnights = 0;
        int whiteBishops = 0, blackBishops = 0;
        int whiteBishopColor = -1, blackBishopColor = -1;

        for (int sq = 0; sq < 64; sq++) {
            Piece piece = board.getPiece(sq);
            if (piece == null) continue;

            PieceType type = piece.getType();
            if (type == PieceType.PAWN || type == PieceType.ROOK || type == PieceType.QUEEN) {
                return false;
            }
            if (type == PieceType.KNIGHT) {
                if (piece.isWhite()) whiteKnights++;
                else blackKnights++;
            } else if (type == PieceType.BISHOP) {
                int sqColor = (Square.file(sq) + Square.rank(sq)) & 1;
                if (piece.isWhite()) {
                    whiteBishops++;
                    whiteBishopColor = sqColor;
                } else {
                    blackBishops++;
                    blackBishopColor = sqColor;
                }
            }
        }

        int whiteMinors = whiteKnights + whiteBishops;
        int blackMinors = blackKnights + blackBishops;

        if (whiteMinors == 0 && blackMinors == 0) return true;
        if (whiteMinors == 1 && blackMinors == 0) return true;
        if (whiteMinors == 0 && blackMinors == 1) return true;
        if (whiteMinors == 1 && blackMinors == 1) {
            if (whiteBishops == 1 && blackBishops == 1) {
                return whiteBishopColor == blackBishopColor;
            }
            if (whiteKnights == 1 && blackKnights == 1) {
                return true;
            }
        }
        return false;
    }

    public static boolean isThreefoldRepetition(Board board, RepetitionTracker tracker) {
        if (tracker == null) return false;
        return tracker.isThreefoldRepetition(board.getZobristHash());
    }

    public static GameState getGameState(Board board) {
        return getGameState(board, null);
    }

    public static GameState getGameState(Board board, RepetitionTracker tracker) {
        List<Move> legalMoves = MoveGenerator.generateLegalMoves(board);
        if (legalMoves.isEmpty()) {
            return board.isInCheck(board.getSideToMove()) ? GameState.CHECKMATE : GameState.STALEMATE;
        }
        if (isThreefoldRepetition(board, tracker)) {
            return GameState.DRAW_THREEFOLD_REPETITION;
        }
        if (isFiftyMoveRule(board)) {
            return GameState.DRAW_FIFTY_MOVES;
        }
        if (isInsufficientMaterial(board)) {
            return GameState.DRAW_INSUFFICIENT_MATERIAL;
        }
        return GameState.ACTIVE;
    }
}
