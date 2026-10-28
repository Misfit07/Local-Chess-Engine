package chess.model;

import java.util.List;

public final class Knight extends Piece {
    private static final int[][] OFFSETS = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2}, {1, 2}, {2, -1}, {2, 1}
    };

    public Knight(PieceColor color) {
        super(PieceType.KNIGHT, color);
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
    }
}
