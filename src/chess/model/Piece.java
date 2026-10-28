package chess.model;

import java.util.List;
import java.util.Objects;

public abstract class Piece {
    private static final Piece[] PIECES = new Piece[12];

    static {
        for (PieceColor c : PieceColor.values()) {
            PIECES[c.ordinal() * 6 + PieceType.PAWN.ordinal()] = new Pawn(c);
            PIECES[c.ordinal() * 6 + PieceType.KNIGHT.ordinal()] = new Knight(c);
            PIECES[c.ordinal() * 6 + PieceType.BISHOP.ordinal()] = new Bishop(c);
            PIECES[c.ordinal() * 6 + PieceType.ROOK.ordinal()] = new Rook(c);
            PIECES[c.ordinal() * 6 + PieceType.QUEEN.ordinal()] = new Queen(c);
            PIECES[c.ordinal() * 6 + PieceType.KING.ordinal()] = new King(c);
        }
    }

    protected final PieceType type;
    protected final PieceColor color;

    protected Piece(PieceType type, PieceColor color) {
        this.type = type;
        this.color = color;
    }

    public static Piece of(PieceType type, PieceColor color) {
        return PIECES[color.ordinal() * 6 + type.ordinal()];
    }

    public static Piece fromChar(char c) {
        PieceColor color = Character.isUpperCase(c) ? PieceColor.WHITE : PieceColor.BLACK;
        char upper = Character.toUpperCase(c);
        for (PieceType t : PieceType.values()) {
            if (t.getSymbol() == upper) {
                return of(t, color);
            }
        }
        return null;
    }

    public PieceType getType() {
        return type;
    }

    public PieceColor getColor() {
        return color;
    }

    public boolean isWhite() {
        return color == PieceColor.WHITE;
    }

    public boolean isBlack() {
        return color == PieceColor.BLACK;
    }

    public char getSymbol() {
        return color == PieceColor.WHITE ? type.getSymbol() : Character.toLowerCase(type.getSymbol());
    }

    public abstract void generatePseudoLegalMoves(Board board, int fromSquare, List<Move> moves);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Piece piece = (Piece) o;
        return type == piece.type && color == piece.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, color);
    }

    @Override
    public String toString() {
        return String.valueOf(getSymbol());
    }
}
