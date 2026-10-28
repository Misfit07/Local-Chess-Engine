package chess.io;

import chess.model.Board;
import chess.model.Piece;
import chess.model.PieceColor;
import chess.model.Square;

public final class Fen {
    public static final String START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private Fen() {}

    public static Board parse(String fen) {
        Board board = new Board();
        board.clear();
        String[] parts = fen.trim().split("\\s+");
        String piecePlacement = parts[0];
        int rank = 7;
        int file = 0;
        for (char c : piecePlacement.toCharArray()) {
            if (c == '/') {
                rank--;
                file = 0;
            } else if (Character.isDigit(c)) {
                file += (c - '0');
            } else {
                Piece piece = Piece.fromChar(c);
                if (piece != null) {
                    board.setPiece(Square.of(file, rank), piece);
                }
                file++;
            }
        }

        if (parts.length > 1) {
            board.setSideToMove("b".equalsIgnoreCase(parts[1]) ? PieceColor.BLACK : PieceColor.WHITE);
        } else {
            board.setSideToMove(PieceColor.WHITE);
        }

        if (parts.length > 2) {
            String castling = parts[2];
            board.setWhiteCastleKingside(castling.contains("K"));
            board.setWhiteCastleQueenside(castling.contains("Q"));
            board.setBlackCastleKingside(castling.contains("k"));
            board.setBlackCastleQueenside(castling.contains("q"));
        }

        if (parts.length > 3) {
            board.setEnPassantSquare(Square.fromName(parts[3]));
        } else {
            board.setEnPassantSquare(Square.NO_SQUARE);
        }

        if (parts.length > 4) {
            try {
                board.setHalfmoveClock(Integer.parseInt(parts[4]));
            } catch (NumberFormatException ignored) {}
        }

        if (parts.length > 5) {
            try {
                board.setFullmoveNumber(Integer.parseInt(parts[5]));
            } catch (NumberFormatException ignored) {}
        }

        board.recomputeHash();
        return board;
    }

    public static String export(Board board) {
        StringBuilder sb = new StringBuilder();
        for (int rank = 7; rank >= 0; rank--) {
            int emptyCount = 0;
            for (int file = 0; file < 8; file++) {
                Piece piece = board.getPiece(file, rank);
                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        sb.append(emptyCount);
                        emptyCount = 0;
                    }
                    sb.append(piece.getSymbol());
                }
            }
            if (emptyCount > 0) {
                sb.append(emptyCount);
            }
            if (rank > 0) {
                sb.append('/');
            }
        }

        sb.append(' ');
        sb.append(board.getSideToMove() == PieceColor.WHITE ? 'w' : 'b');

        sb.append(' ');
        StringBuilder castling = new StringBuilder();
        if (board.canWhiteCastleKingside()) castling.append('K');
        if (board.canWhiteCastleQueenside()) castling.append('Q');
        if (board.canBlackCastleKingside()) castling.append('k');
        if (board.canBlackCastleQueenside()) castling.append('q');
        if (castling.length() == 0) castling.append('-');
        sb.append(castling);

        sb.append(' ');
        int ep = board.getEnPassantSquare();
        sb.append(ep != Square.NO_SQUARE ? Square.name(ep) : "-");

        sb.append(' ');
        sb.append(board.getHalfmoveClock());

        sb.append(' ');
        sb.append(board.getFullmoveNumber());

        return sb.toString();
    }
}
