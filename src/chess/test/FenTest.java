package chess.test;

import chess.io.Fen;
import chess.model.Board;
import chess.model.Piece;
import chess.model.PieceColor;
import chess.model.PieceType;
import chess.model.Square;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FenTest {
    private static final String START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private static final String KIWIPETE_FEN = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";
    private static final String POS3_FEN = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1";
    private static final String POS4_FEN = "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1";
    private static final String POS5_FEN = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8";
    private static final String POS6_FEN = "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10";

    @Test
    public void testStartingPositionRoundTrip() {
        Board board = Fen.parse(START_FEN);
        assertEquals(START_FEN, Fen.export(board));
        assertEquals(PieceColor.WHITE, board.getSideToMove());
        assertTrue(board.canWhiteCastleKingside());
        assertTrue(board.canWhiteCastleQueenside());
        assertTrue(board.canBlackCastleKingside());
        assertTrue(board.canBlackCastleQueenside());
        assertEquals(Square.NO_SQUARE, board.getEnPassantSquare());
        assertEquals(0, board.getHalfmoveClock());
        assertEquals(1, board.getFullmoveNumber());
        assertEquals(Piece.of(PieceType.ROOK, PieceColor.WHITE), board.getPiece(Square.A1));
        assertEquals(Piece.of(PieceType.KING, PieceColor.WHITE), board.getPiece(Square.E1));
        assertEquals(Piece.of(PieceType.KING, PieceColor.BLACK), board.getPiece(Square.E8));
    }

    @Test
    public void testKiwipeteRoundTrip() {
        Board board = Fen.parse(KIWIPETE_FEN);
        assertEquals(KIWIPETE_FEN, Fen.export(board));
    }

    @Test
    public void testStandardPositionsRoundTrip() {
        assertEquals(POS3_FEN, Fen.export(Fen.parse(POS3_FEN)));
        assertEquals(POS4_FEN, Fen.export(Fen.parse(POS4_FEN)));
        assertEquals(POS5_FEN, Fen.export(Fen.parse(POS5_FEN)));
        assertEquals(POS6_FEN, Fen.export(Fen.parse(POS6_FEN)));
    }

    @Test
    public void testEnPassantTargetParsed() {
        String fenWithEp = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1";
        Board board = Fen.parse(fenWithEp);
        assertEquals(Square.fromName("e3"), board.getEnPassantSquare());
        assertEquals(PieceColor.BLACK, board.getSideToMove());
        assertEquals(fenWithEp, Fen.export(board));
    }
}
