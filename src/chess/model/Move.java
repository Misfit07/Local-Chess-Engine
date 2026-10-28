package chess.model;

import java.util.Objects;

public final class Move {
    private final int from;
    private final int to;
    private final Piece pieceMoved;
    private final Piece capturedPiece;
    private final Piece promotionPiece;
    private final MoveFlag flag;

    public Move(int from, int to, Piece pieceMoved) {
        this(from, to, pieceMoved, null, null, MoveFlag.NORMAL);
    }

    public Move(int from, int to, Piece pieceMoved, Piece capturedPiece) {
        this(from, to, pieceMoved, capturedPiece, null, MoveFlag.NORMAL);
    }

    public Move(int from, int to, Piece pieceMoved, Piece capturedPiece, Piece promotionPiece, MoveFlag flag) {
        this.from = from;
        this.to = to;
        this.pieceMoved = pieceMoved;
        this.capturedPiece = capturedPiece;
        this.promotionPiece = promotionPiece;
        this.flag = flag;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public Piece getPieceMoved() {
        return pieceMoved;
    }

    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    public Piece getPromotionPiece() {
        return promotionPiece;
    }

    public MoveFlag getFlag() {
        return flag;
    }

    public boolean isCapture() {
        return capturedPiece != null;
    }

    public boolean isCastle() {
        return flag == MoveFlag.CASTLE;
    }

    public boolean isEnPassant() {
        return flag == MoveFlag.EN_PASSANT;
    }

    public boolean isPromotion() {
        return flag == MoveFlag.PROMOTION;
    }

    public String toUci() {
        StringBuilder sb = new StringBuilder();
        sb.append(Square.name(from));
        sb.append(Square.name(to));
        if (promotionPiece != null) {
            sb.append(Character.toLowerCase(promotionPiece.getType().getSymbol()));
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return from == move.from
                && to == move.to
                && flag == move.flag
                && Objects.equals(pieceMoved, move.pieceMoved)
                && Objects.equals(capturedPiece, move.capturedPiece)
                && Objects.equals(promotionPiece, move.promotionPiece);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, pieceMoved, capturedPiece, promotionPiece, flag);
    }

    @Override
    public String toString() {
        return toUci();
    }
}
