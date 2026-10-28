package chess.test;

import chess.io.Fen;
import chess.io.Uci;
import chess.model.Board;
import chess.model.PieceColor;
import chess.model.PieceType;
import chess.model.Square;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class UciTest {

    @Test
    public void testUciAndIsReadyCommands() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Uci uci = new Uci(new ByteArrayInputStream(new byte[0]), new PrintStream(out));

        uci.processCommand("uci");
        String output = out.toString();
        assertTrue(output.contains("id name JavaChessEngine"));
        assertTrue(output.contains("uciok"));

        out.reset();
        uci.processCommand("isready");
        assertEquals("readyok\r\n", out.toString().replaceAll("\r?\n", "\r\n"));
    }

    @Test
    public void testPositionStartposAndMoves() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Uci uci = new Uci(new ByteArrayInputStream(new byte[0]), new PrintStream(out));

        uci.processCommand("position startpos moves e2e4 e7e5 g1f3");
        Board board = uci.getBoard();

        assertEquals(PieceColor.BLACK, board.getSideToMove());
        assertEquals(chess.model.Piece.of(PieceType.PAWN, PieceColor.WHITE), board.getPiece(Square.fromName("e4")));
        assertEquals(chess.model.Piece.of(PieceType.PAWN, PieceColor.BLACK), board.getPiece(Square.fromName("e5")));
        assertEquals(chess.model.Piece.of(PieceType.KNIGHT, PieceColor.WHITE), board.getPiece(Square.fromName("f3")));
    }

    @Test
    public void testPositionFenAndGoDepth() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Uci uci = new Uci(new ByteArrayInputStream(new byte[0]), new PrintStream(out));

        uci.processCommand("position fen r1bqkb1r/pppp1ppp/2n5/4p3/2B1n3/5Q2/PPPP1PPP/RNB1K1NR w KQkq - 0 4");
        uci.processCommand("go depth 2");

        String output = out.toString();
        assertTrue(output.contains("bestmove f3f7"));
    }

    @Test
    public void testGoMovetime() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Uci uci = new Uci(new ByteArrayInputStream(new byte[0]), new PrintStream(out));

        uci.processCommand("position startpos");
        uci.processCommand("go movetime 150");

        String output = out.toString();
        assertTrue(output.contains("bestmove "));
    }

    @Test
    public void testUcinewgame() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Uci uci = new Uci(new ByteArrayInputStream(new byte[0]), new PrintStream(out));

        uci.processCommand("position startpos moves e2e4");
        assertEquals(PieceColor.BLACK, uci.getBoard().getSideToMove());

        uci.processCommand("ucinewgame");
        assertEquals(Fen.START_FEN, Fen.export(uci.getBoard()));
    }
}
