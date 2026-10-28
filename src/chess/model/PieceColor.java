package chess.model;

public enum PieceColor {
    WHITE,
    BLACK;

    public PieceColor opposite() {
        return this == WHITE ? BLACK : WHITE;
    }

    public int pawnDirection() {
        return this == WHITE ? 1 : -1;
    }

    public int pawnStartRank() {
        return this == WHITE ? 1 : 6;
    }

    public int pawnPromotionRank() {
        return this == WHITE ? 7 : 0;
    }
}
