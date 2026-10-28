package chess.model;

import java.util.List;

public final class Pawn extends Piece {
    private static final PieceType[] PROMOTIONS = {
            PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT
    };

    public Pawn(PieceColor color) {
        super(PieceType.PAWN, color);
    }

    @Override
    public void generatePseudoLegalMoves(Board board, int fromSquare, List<Move> moves) {
        int file = Square.file(fromSquare);
        int rank = Square.rank(fromSquare);
        int direction = color.pawnDirection();
        int nextRank = rank + direction;

        if (Square.isValid(file, nextRank)) {
            int oneStep = Square.of(file, nextRank);
            if (board.getPiece(oneStep) == null) {
                if (nextRank == color.pawnPromotionRank()) {
                    for (PieceType promo : PROMOTIONS) {
                        moves.add(new Move(fromSquare, oneStep, this, null, Piece.of(promo, color), MoveFlag.PROMOTION));
                    }
                } else {
                    moves.add(new Move(fromSquare, oneStep, this));
                    if (rank == color.pawnStartRank()) {
                        int twoStep = Square.of(file, rank + 2 * direction);
                        if (board.getPiece(twoStep) == null) {
                            moves.add(new Move(fromSquare, twoStep, this));
                        }
                    }
                }
            }
        }

        int[] captureFiles = {file - 1, file + 1};
        for (int capFile : captureFiles) {
            if (capFile >= 0 && capFile < 8 && Square.isValid(capFile, nextRank)) {
                int capSquare = Square.of(capFile, nextRank);
                Piece target = board.getPiece(capSquare);
                if (target != null && target.getColor() == color.opposite()) {
                    if (nextRank == color.pawnPromotionRank()) {
                        for (PieceType promo : PROMOTIONS) {
                            moves.add(new Move(fromSquare, capSquare, this, target, Piece.of(promo, color), MoveFlag.PROMOTION));
                        }
                    } else {
                        moves.add(new Move(fromSquare, capSquare, this, target));
                    }
                } else if (capSquare == board.getEnPassantSquare()) {
                    int capturedPawnSquare = Square.of(capFile, rank);
                    Piece capturedPawn = board.getPiece(capturedPawnSquare);
                    if (capturedPawn != null && capturedPawn.getColor() == color.opposite() && capturedPawn.getType() == PieceType.PAWN) {
                        moves.add(new Move(fromSquare, capSquare, this, capturedPawn, null, MoveFlag.EN_PASSANT));
                    }
                }
            }
        }
    }
}
