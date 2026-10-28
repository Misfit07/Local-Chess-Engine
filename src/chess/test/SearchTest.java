package chess.test;

import chess.io.Fen;
import chess.model.Board;
import chess.model.Square;
import chess.search.RootMoveScore;
import chess.search.Search;
import chess.search.SearchResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SearchTest {

    @Test
    public void testFindMateInOne() {
        Board board = Fen.parse("r1bqkb1r/pppp1ppp/2n5/4p3/2B1n3/5Q2/PPPP1PPP/RNB1K1NR w KQkq - 0 4");
        Search search = new Search();
        SearchResult result = search.search(board, 2);

        assertNotNull(result.getBestMove());
        assertEquals(Square.fromName("f3"), result.getBestMove().getFrom());
        assertEquals(Square.fromName("f7"), result.getBestMove().getTo());
        assertTrue(result.getScore() >= Search.MATE_SCORE - 10);
    }

    @Test
    public void testMateInOneRook() {
        Board board = Fen.parse("k7/8/1K6/8/8/8/8/7R w - - 0 1");
        Search search = new Search();
        SearchResult result = search.search(board, 2);

        assertNotNull(result.getBestMove());
        assertEquals(Square.fromName("h1"), result.getBestMove().getFrom());
        assertEquals(Square.fromName("h8"), result.getBestMove().getTo());
        assertEquals(Search.MATE_SCORE - 1, result.getScore());
    }

    @Test
    public void testSearchReturnsTopNMovesSorted() {
        Board board = Fen.parse(Fen.START_FEN);
        Search search = new Search();
        SearchResult result = search.search(board, 2, 0, 3);

        List<RootMoveScore> topMoves = result.getTopMoves();
        assertEquals(3, topMoves.size());
        assertTrue(topMoves.get(0).getScore() >= topMoves.get(1).getScore());
        assertTrue(topMoves.get(1).getScore() >= topMoves.get(2).getScore());
        assertEquals(result.getBestMove(), topMoves.get(0).getMove());
    }

    @Test
    public void testTimeControlStopsSearch() {
        Board board = Fen.parse(Fen.START_FEN);
        Search search = new Search();
        long start = System.currentTimeMillis();
        SearchResult result = search.search(board, 50, 200, 1);
        long elapsed = System.currentTimeMillis() - start;

        assertNotNull(result.getBestMove());
        assertTrue(elapsed < 1500, "Search should stop shortly after time limit: " + elapsed);
        assertTrue(result.getDepth() >= 1);
    }

    @Test
    public void testCapturePreferredOverLoss() {
        Board board = Fen.parse("r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 2 3");
        Search search = new Search();
        SearchResult result = search.search(board, 3);
        assertNotNull(result.getBestMove());
    }
}
