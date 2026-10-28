package chess.movegen;

import chess.model.Board;
import chess.model.Piece;
import chess.model.PieceColor;
import chess.model.PieceType;
import chess.model.Square;

public final class AttackDetector {
    private static final int[][] KNIGHT_OFFSETS = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2}, {1, 2}, {2, -1}, {2, 1}
    };

    private static final int[][] KING_OFFSETS = {
            {-1, -1}, {-1, 0}, {-1, 1}, {0, -1},
            {0, 1}, {1, -1}, {1, 0}, {1, 1}
    };

    private static final int[][] ORTHOGONAL_DIRECTIONS = {
            {0, 1}, {0, -1}, {1, 0}, {-1, 0}
    };

    private static final int[][] DIAGONAL_DIRECTIONS = {
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };

    private AttackDetector() {}

    public static boolean isSquareAttacked(Board board, int square, PieceColor bySide) {
        int file = Square.file(square);
        int rank = Square.rank(square);

        int pawnSourceRank = rank - bySide.pawnDirection();
        if (pawnSourceRank >= 0 && pawnSourceRank < 8) {
            if (file > 0) {
                Piece p = board.getPiece(Square.of(file - 1, pawnSourceRank));
                if (p != null && p.getColor() == bySide && p.getType() == PieceType.PAWN) {
                    return true;
                }
            }
            if (file < 7) {
                Piece p = board.getPiece(Square.of(file + 1, pawnSourceRank));
                if (p != null && p.getColor() == bySide && p.getType() == PieceType.PAWN) {
                    return true;
                }
            }
        }

        for (int[] offset : KNIGHT_OFFSETS) {
            int tf = file + offset[0];
            int tr = rank + offset[1];
            if (Square.isValid(tf, tr)) {
                Piece p = board.getPiece(Square.of(tf, tr));
                if (p != null && p.getColor() == bySide && p.getType() == PieceType.KNIGHT) {
                    return true;
                }
            }
        }

        for (int[] offset : KING_OFFSETS) {
            int tf = file + offset[0];
            int tr = rank + offset[1];
            if (Square.isValid(tf, tr)) {
                Piece p = board.getPiece(Square.of(tf, tr));
                if (p != null && p.getColor() == bySide && p.getType() == PieceType.KING) {
                    return true;
                }
            }
        }

        for (int[] dir : ORTHOGONAL_DIRECTIONS) {
            int tf = file + dir[0];
            int tr = rank + dir[1];
            while (Square.isValid(tf, tr)) {
                Piece p = board.getPiece(Square.of(tf, tr));
                if (p != null) {
                    if (p.getColor() == bySide && (p.getType() == PieceType.ROOK || p.getType() == PieceType.QUEEN)) {
                        return true;
                    }
                    break;
                }
                tf += dir[0];
                tr += dir[1];
            }
        }

        for (int[] dir : DIAGONAL_DIRECTIONS) {
            int tf = file + dir[0];
            int tr = rank + dir[1];
            while (Square.isValid(tf, tr)) {
                Piece p = board.getPiece(Square.of(tf, tr));
                if (p != null) {
                    if (p.getColor() == bySide && (p.getType() == PieceType.BISHOP || p.getType() == PieceType.QUEEN)) {
                        return true;
                    }
                    break;
                }
                tf += dir[0];
                tr += dir[1];
            }
        }

        return false;
    }
}
