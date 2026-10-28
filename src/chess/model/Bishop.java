package chess.model;

import java.util.List;

public final class Bishop extends Piece {
    private static final int[][] DIRECTIONS = {
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };

    public Bishop(PieceColor color) {
        super(PieceType.BISHOP, color);
    }

    @Override
    public void generatePseudoLegalMoves(Board board, int fromSquare, List<Move> moves) {
        int file = Square.file(fromSquare);
        int rank = Square.rank(fromSquare);

        for (int[] dir : DIRECTIONS) {
            int currentFile = file + dir[0];
            int currentRank = rank + dir[1];
            while (Square.isValid(currentFile, currentRank)) {
                int targetSquare = Square.of(currentFile, currentRank);
                Piece target = board.getPiece(targetSquare);
                if (target == null) {
                    moves.add(new Move(fromSquare, targetSquare, this));
                } else {
                    if (target.getColor() == color.opposite()) {
                        moves.add(new Move(fromSquare, targetSquare, this, target));
                    }
                    break;
                }
                currentFile += dir[0];
                currentRank += dir[1];
            }
        }
    }
}
