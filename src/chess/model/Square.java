package chess.model;

public final class Square {
    public static final int NO_SQUARE = -1;

    public static final int A1 = 0, B1 = 1, C1 = 2, D1 = 3, E1 = 4, F1 = 5, G1 = 6, H1 = 7;
    public static final int A8 = 56, B8 = 57, C8 = 58, D8 = 59, E8 = 60, F8 = 61, G8 = 62, H8 = 63;

    private Square() {}

    public static int of(int file, int rank) {
        return rank * 8 + file;
    }

    public static int file(int square) {
        return square & 7;
    }

    public static int rank(int square) {
        return square >> 3;
    }

    public static boolean isValid(int square) {
        return square >= 0 && square < 64;
    }

    public static boolean isValid(int file, int rank) {
        return file >= 0 && file < 8 && rank >= 0 && rank < 8;
    }

    public static String name(int square) {
        if (!isValid(square)) {
            return "-";
        }
        return "" + (char) ('a' + file(square)) + (char) ('1' + rank(square));
    }

    public static int fromName(String name) {
        if (name == null || name.length() != 2 || "-".equals(name)) {
            return NO_SQUARE;
        }
        int file = name.charAt(0) - 'a';
        int rank = name.charAt(1) - '1';
        if (!isValid(file, rank)) {
            return NO_SQUARE;
        }
        return of(file, rank);
    }
}
