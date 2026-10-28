package chess.test;

import chess.hash.RepetitionTracker;
import chess.io.Fen;
import chess.model.Board;
import chess.model.GameState;
import chess.model.GameStateDetector;
import chess.model.Move;
import chess.model.PieceColor;
import chess.model.Square;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameStateTest {

    @Test
    public void testCheckDetection() {
        Board normalBoard = Fen.parse(Fen.START_FEN);
        assertFalse(normalBoard.isInCheck(PieceColor.WHITE));
        assertFalse(normalBoard.isInCheck(PieceColor.BLACK));

        Board checkBoard = Fen.parse("rnbqk1nr/pppp1ppp/4p3/8/1b1P4/5N2/PPP1PPPP/RNBQKB1R w KQkq - 2 3");
        assertTrue(checkBoard.isInCheck(PieceColor.WHITE));
        assertFalse(checkBoard.isInCheck(PieceColor.BLACK));
    }

    @Test
    public void testFoolsMateCheckmate() {
        Board board = Fen.parse("rnb1kbnr/pppp1ppp/4p3/8/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3");
        assertTrue(board.isInCheck(PieceColor.WHITE));
        assertTrue(GameStateDetector.isCheckmate(board));
        assertFalse(GameStateDetector.isStalemate(board));
        assertEquals(GameState.CHECKMATE, GameStateDetector.getGameState(board));
    }

    @Test
    public void testScholarsMateCheckmate() {
        Board board = Fen.parse("r1bqkb1r/pppp1Qpp/2n2n2/4p3/2B1P3/8/PPPP1PPP/RNB1K1NR b KQkq - 0 4");
        assertTrue(board.isInCheck(PieceColor.BLACK));
        assertTrue(GameStateDetector.isCheckmate(board));
        assertFalse(GameStateDetector.isStalemate(board));
        assertEquals(GameState.CHECKMATE, GameStateDetector.getGameState(board));
    }

    @Test
    public void testStalemate() {
        Board board = Fen.parse("k7/8/1Q6/8/8/8/8/K7 b - - 0 1");
        assertFalse(board.isInCheck(PieceColor.BLACK));
        assertTrue(GameStateDetector.isStalemate(board));
        assertFalse(GameStateDetector.isCheckmate(board));
        assertEquals(GameState.STALEMATE, GameStateDetector.getGameState(board));
    }

    @Test
    public void testFiftyMoveRule() {
        Board board = Fen.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 100 50");
        assertTrue(GameStateDetector.isFiftyMoveRule(board));
        assertEquals(GameState.DRAW_FIFTY_MOVES, GameStateDetector.getGameState(board));
    }

    @Test
    public void testInsufficientMaterial() {
        assertTrue(GameStateDetector.isInsufficientMaterial(Fen.parse("8/8/8/4k3/8/8/4K3/8 w - - 0 1")));
        assertTrue(GameStateDetector.isInsufficientMaterial(Fen.parse("8/8/8/4k3/8/8/4KB2/8 w - - 0 1")));
        assertTrue(GameStateDetector.isInsufficientMaterial(Fen.parse("8/8/8/4k3/8/8/4KN2/8 w - - 0 1")));
        assertTrue(GameStateDetector.isInsufficientMaterial(Fen.parse("8/8/5b2/4k3/8/2B5/4K3/8 w - - 0 1")));
        assertFalse(GameStateDetector.isInsufficientMaterial(Fen.parse("8/8/4b3/4k3/8/2B5/4K3/8 w - - 0 1")));
        assertFalse(GameStateDetector.isInsufficientMaterial(Fen.parse("8/8/8/4k3/4P3/8/4K3/8 w - - 0 1")));
        assertFalse(GameStateDetector.isInsufficientMaterial(Fen.parse("8/8/8/4k3/8/8/4KR2/8 w - - 0 1")));
    }

    @Test
    public void testThreefoldRepetition() {
        Board board = Fen.parse(Fen.START_FEN);
        RepetitionTracker tracker = new RepetitionTracker();

        tracker.addGamePosition(board.getZobristHash());

        Move wNf3 = new Move(Square.fromName("g1"), Square.fromName("f3"), board.getPiece(Square.fromName("g1")));
        board.makeMove(wNf3);
        tracker.addGamePosition(board.getZobristHash());

        Move bNf6 = new Move(Square.fromName("g8"), Square.fromName("f6"), board.getPiece(Square.fromName("g8")));
        board.makeMove(bNf6);
        tracker.addGamePosition(board.getZobristHash());

        Move wNg1 = new Move(Square.fromName("f3"), Square.fromName("g1"), board.getPiece(Square.fromName("f3")));
        board.makeMove(wNg1);
        tracker.addGamePosition(board.getZobristHash());

        Move bNg8 = new Move(Square.fromName("f6"), Square.fromName("g8"), board.getPiece(Square.fromName("f6")));
        board.makeMove(bNg8);
        tracker.addGamePosition(board.getZobristHash());

        assertEquals(2, tracker.getRepetitionCount(board.getZobristHash()));
        assertFalse(tracker.isThreefoldRepetition(board.getZobristHash()));

        board.makeMove(wNf3);
        tracker.addGamePosition(board.getZobristHash());
        board.makeMove(bNf6);
        tracker.addGamePosition(board.getZobristHash());
        board.makeMove(wNg1);
        tracker.addGamePosition(board.getZobristHash());
        board.makeMove(bNg8);
        tracker.addGamePosition(board.getZobristHash());

        assertEquals(3, tracker.getRepetitionCount(board.getZobristHash()));
        assertTrue(tracker.isThreefoldRepetition(board.getZobristHash()));
        assertEquals(GameState.DRAW_THREEFOLD_REPETITION, GameStateDetector.getGameState(board, tracker));
    }
}
