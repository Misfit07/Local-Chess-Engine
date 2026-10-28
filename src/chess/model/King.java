package chess.model;

import chess.movegen.AttackDetector;

import java.util.List;

public final class King extends Piece {
    private static final int[][] OFFSETS = {
            {-1, -1}, {-1, 0}, {-1, 1}, {0, -1},
            {0, 1}, {1, -1}, {1, 0}, {1, 1}
    };

    public King(PieceColor color) {
        super(PieceType.KING, color);
    }

    @Override
    public void generatePseudoLegalMoves(Board board, int fromSquare, List<Move> moves) {
        int file = Square.file(fromSquare);
        int rank = Square.rank(fromSquare);

        for (int[] offset : OFFSETS) {
            int targetFile = file + offset[0];
            int targetRank = rank + offset[1];
            if (Square.isValid(targetFile, targetRank)) {
                int targetSquare = Square.of(targetFile, targetRank);
                Piece target = board.getPiece(targetSquare);
                if (target == null) {
                    moves.add(new Move(fromSquare, targetSquare, this));
                } else if (target.getColor() == color.opposite()) {
                    moves.add(new Move(fromSquare, targetSquare, this, target));
                }
            }
        }

        generateCastlingMoves(board, fromSquare, moves);
    }

    private void generateCastlingMoves(Board board, int fromSquare, List<Move> moves) {
        PieceColor opponent = color.opposite();
        if (color == PieceColor.WHITE && fromSquare == Square.E1) {
            if (board.canWhiteCastleKingside()
                    && board.getPiece(Square.F1) == null
                    && board.getPiece(Square.G1) == null
                    && board.getPiece(Square.H1) == Piece.of(PieceType.ROOK, PieceColor.WHITE)
                    && !AttackDetector.isSquareAttacked(board, Square.E1, opponent)
                    && !AttackDetector.isSquareAttacked(board, Square.F1, opponent)
                    && !AttackDetector.isSquareAttacked(board, Square.G1, opponent)) {
                moves.add(new Move(Square.E1, Square.G1, this, null, null, MoveFlag.CASTLE));
            }
            if (board.canWhiteCastleQueenside()
                    && board.getPiece(Square.D1) == null
                    && board.getPiece(Square.C1) == null
                    && board.getPiece(Square.B1) == null
                    && board.getPiece(Square.A1) == Piece.of(PieceType.ROOK, PieceColor.WHITE)
                    && !AttackDetector.isSquareAttacked(board, Square.E1, opponent)
                    && !AttackDetector.isSquareAttacked(board, Square.D1, opponent)
                    && !AttackDetector.isSquareAttacked(board, Square.C1, opponent)) {
                moves.add(new Move(Square.E1, Square.C1, this, null, null, MoveFlag.CASTLE));
            }
        } else if (color == PieceColor.BLACK && fromSquare == Square.E8) {
            if (board.canBlackCastleKingside()
                    && board.getPiece(Square.F8) == null
                    && board.getPiece(Square.G8) == null
                    && board.getPiece(Square.H8) == Piece.of(PieceType.ROOK, PieceColor.BLACK)
                    && !AttackDetector.isSquareAttacked(board, Square.E8, opponent)
                    && !AttackDetector.isSquareAttacked(board, Square.F8, opponent)
                    && !AttackDetector.isSquareAttacked(board, Square.G8, opponent)) {
                moves.add(new Move(Square.E8, Square.G8, this, null, null, MoveFlag.CASTLE));
            }
            if (board.canBlackCastleQueenside()
                    && board.getPiece(Square.D8) == null
                    && board.getPiece(Square.C8) == null
                    && board.getPiece(Square.B8) == null
                    && board.getPiece(Square.A8) == Piece.of(PieceType.ROOK, PieceColor.BLACK)
                    && !AttackDetector.isSquareAttacked(board, Square.E8, opponent)
                    && !AttackDetector.isSquareAttacked(board, Square.D8, opponent)
                    && !AttackDetector.isSquareAttacked(board, Square.C8, opponent)) {
                moves.add(new Move(Square.E8, Square.C8, this, null, null, MoveFlag.CASTLE));
            }
        }
    }
}
