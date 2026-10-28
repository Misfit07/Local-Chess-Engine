package chess.hash;

import chess.model.Board;
import chess.model.Piece;
import chess.model.PieceColor;
import chess.model.Square;

import java.util.Random;

public final class Zobrist {
    private static final long[][] PIECE_KEYS = new long[12][64];
    private static final long SIDE_KEY;
    private static final long[] CASTLE_KEYS = new long[4];
    private static final long[] EN_PASSANT_KEYS = new long[8];

    static {
        Random rng = new Random(1070372L);
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 64; j++) {
                PIECE_KEYS[i][j] = rng.nextLong();
            }
        }
        SIDE_KEY = rng.nextLong();
        for (int i = 0; i < 4; i++) {
            CASTLE_KEYS[i] = rng.nextLong();
        }
        for (int i = 0; i < 8; i++) {
            EN_PASSANT_KEYS[i] = rng.nextLong();
        }
    }

    private Zobrist() {}

    public static long pieceKey(Piece piece, int square) {
        if (piece == null || !Square.isValid(square)) {
            return 0L;
        }
        int index = piece.getColor().ordinal() * 6 + piece.getType().ordinal();
        return PIECE_KEYS[index][square];
    }

    public static long sideKey() {
        return SIDE_KEY;
    }

    public static long castleKey(int index) {
        return CASTLE_KEYS[index];
    }

    public static long enPassantKey(int file) {
        if (file < 0 || file >= 8) {
            return 0L;
        }
        return EN_PASSANT_KEYS[file];
    }

    public static long computeHash(Board board) {
        long hash = 0L;
        for (int sq = 0; sq < 64; sq++) {
            Piece piece = board.getPiece(sq);
            if (piece != null) {
                hash ^= pieceKey(piece, sq);
            }
        }
        if (board.getSideToMove() == PieceColor.BLACK) {
            hash ^= SIDE_KEY;
        }
        if (board.canWhiteCastleKingside()) {
            hash ^= CASTLE_KEYS[0];
        }
        if (board.canWhiteCastleQueenside()) {
            hash ^= CASTLE_KEYS[1];
        }
        if (board.canBlackCastleKingside()) {
            hash ^= CASTLE_KEYS[2];
        }
        if (board.canBlackCastleQueenside()) {
            hash ^= CASTLE_KEYS[3];
        }
        int ep = board.getEnPassantSquare();
        if (ep != Square.NO_SQUARE) {
            hash ^= EN_PASSANT_KEYS[Square.file(ep)];
        }
        return hash;
    }
}
