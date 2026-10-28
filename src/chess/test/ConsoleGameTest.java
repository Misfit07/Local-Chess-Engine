package chess.test;

import chess.io.Fen;
import chess.model.Board;
import chess.model.Move;
import chess.model.PieceColor;
import chess.model.PieceType;
import chess.model.Square;
import chess.ui.ConsoleGame;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class ConsoleGameTest {

    @Test
    public void testRenderBoard() {
        Board board = Fen.parse(Fen.START_FEN);
        String rendering = ConsoleGame.renderBoard(board);
        assertNotNull(rendering);
        assertTrue(rendering.contains("a   b   c   d   e   f   g   h"));
        assertTrue(rendering.contains("r | n | b | q | k | b | n | r"));
        assertTrue(rendering.contains("R | N | B | Q | K | B | N | R"));
    }

    @Test
    public void testParseValidMove() {
        ConsoleGame game = new ConsoleGame();
        Move move = game.parseMove("e2e4");
        assertNotNull(move);
        assertEquals(Square.fromName("e2"), move.getFrom());
        assertEquals(Square.fromName("e4"), move.getTo());
    }

    @Test
    public void testParseInvalidMove() {
        ConsoleGame game = new ConsoleGame();
        assertNull(game.parseMove("e2e5"));
        assertNull(game.parseMove("invalid"));
    }

    @Test
    public void testParsePromotionDefaultQueen() {
        ByteArrayInputStream in = new ByteArrayInputStream("quit\n".getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ConsoleGame game = new ConsoleGame(in, new PrintStream(out));

        Board board = game.getBoard();
        board.clear();
        board.setPiece(Square.fromName("e7"), chess.model.Piece.of(PieceType.PAWN, PieceColor.WHITE));
        board.setPiece(Square.fromName("e1"), chess.model.Piece.of(PieceType.KING, PieceColor.WHITE));
        board.setPiece(Square.fromName("h8"), chess.model.Piece.of(PieceType.KING, PieceColor.BLACK));
        board.setSideToMove(PieceColor.WHITE);

        Move promoMove = game.parseMove("e7e8");
        assertNotNull(promoMove);
        assertTrue(promoMove.isPromotion());
        assertEquals(PieceType.QUEEN, promoMove.getPromotionPiece().getType());
    }

    @Test
    public void testInteractiveGameLoop() {
        String simulatedCommands = "eval\nmoves\ne2e4\nundo\nquit\n";
        ByteArrayInputStream in = new ByteArrayInputStream(simulatedCommands.getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        ConsoleGame game = new ConsoleGame(in, new PrintStream(out));
        game.setHumanColor(PieceColor.WHITE);
        game.run();

        String output = out.toString();
        assertTrue(output.contains("Evaluation:"));
        assertTrue(output.contains("Legal moves"));
    }
}
