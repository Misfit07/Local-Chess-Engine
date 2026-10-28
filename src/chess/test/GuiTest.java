package chess.test;

import chess.io.Fen;
import chess.model.PieceColor;
import chess.ui.BoardPanel;
import chess.ui.ChessFrame;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GuiTest {

    @Test
    public void testChessFrameCreation() {
        ChessFrame frame = new ChessFrame();
        assertNotNull(frame.getBoardPanel());
        assertNotNull(frame.getBoardPanel().getBoard());
        assertEquals(PieceColor.WHITE, frame.getBoardPanel().getBoard().getSideToMove());
        frame.dispose();
    }

    @Test
    public void testBoardPanelFlipping() {
        BoardPanel panel = new BoardPanel();
        assertFalse(panel.isFlipped());
        panel.setFlipped(true);
        assertTrue(panel.isFlipped());
        panel.setFlipped(false);
        assertFalse(panel.isFlipped());
    }

    @Test
    public void testBoardPanelHumanSide() {
        BoardPanel panel = new BoardPanel();
        assertEquals(PieceColor.WHITE, panel.getHumanSide());
        panel.setHumanSide(PieceColor.BLACK);
        assertEquals(PieceColor.BLACK, panel.getHumanSide());
        panel.setHumanSide(null);
        assertNull(panel.getHumanSide());
    }

    @Test
    public void testBoardPanelSetBoard() {
        BoardPanel panel = new BoardPanel();
        String fen = "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1";
        panel.setBoard(Fen.parse(fen), new chess.hash.RepetitionTracker());
        assertEquals(fen, Fen.export(panel.getBoard()));
    }
}
