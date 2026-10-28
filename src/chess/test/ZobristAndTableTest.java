package chess.test;

import chess.hash.TranspositionTable;
import chess.hash.Zobrist;
import chess.io.Fen;
import chess.model.Board;
import chess.model.Move;
import chess.model.PieceColor;
import chess.model.PieceType;
import chess.model.Square;
import chess.movegen.MoveGenerator;
import chess.search.Search;
import chess.search.SearchResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class ZobristAndTableTest {

    @Test
    public void testTranspositionTableStoreAndProbe() {
        TranspositionTable tt = new TranspositionTable(1024);
        long hash = 0x123456789ABCDEFL;
        Move move = new Move(Square.fromName("e2"), Square.fromName("e4"), chess.model.Piece.of(PieceType.PAWN, PieceColor.WHITE));

        assertNull(tt.probe(hash));

        tt.store(hash, 4, 150, TranspositionTable.EXACT, move, 0);
        TranspositionTable.Entry entry = tt.probe(hash);

        assertNotNull(entry);
        assertEquals(hash, entry.hash);
        assertEquals(4, entry.depth);
        assertEquals(150, entry.getScore(0));
        assertEquals(TranspositionTable.EXACT, entry.flag);
        assertEquals(move, entry.move);

        assertNull(tt.probe(0x99999999L));
    }

    @Test
    public void testMateScoreDistanceAdjustmentInTable() {
        TranspositionTable tt = new TranspositionTable(1024);
        long hash = 0xCAFEBABEL;
        int plyFound = 2;
        int mateScoreAtPly2 = Search.MATE_SCORE - plyFound;

        tt.store(hash, 5, mateScoreAtPly2, TranspositionTable.EXACT, null, plyFound);

        TranspositionTable.Entry entry = tt.probe(hash);
        assertNotNull(entry);

        int scoreAtPly4 = entry.getScore(4);
        assertEquals(Search.MATE_SCORE - 4, scoreAtPly4);
    }

    @Test
    public void testIncrementalHashEqualsFromScratchAfterRandomMoves() {
        String[] testPositions = {
                Fen.START_FEN,
                "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
                "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1"
        };

        Random rng = new Random(42L);

        for (String fen : testPositions) {
            Board board = Fen.parse(fen);
            int movesCount = 0;

            for (int step = 0; step < 40; step++) {
                List<Move> legalMoves = MoveGenerator.generateLegalMoves(board);
                if (legalMoves.isEmpty()) {
                    break;
                }
                Move move = legalMoves.get(rng.nextInt(legalMoves.size()));
                board.makeMove(move);
                movesCount++;

                assertEquals(Zobrist.computeHash(board), board.getZobristHash());
            }

            for (int i = 0; i < movesCount; i++) {
                board.unmakeMove();
                assertEquals(Zobrist.computeHash(board), board.getZobristHash());
            }

            assertEquals(fen, Fen.export(board));
        }
    }

    @Test
    public void testSearchWithTranspositionTableEfficiency() {
        Board board = Fen.parse(Fen.START_FEN);
        Search search = new Search();

        SearchResult r1 = search.search(board, 4);
        long nodes1 = r1.getNodes();

        SearchResult r2 = search.search(board, 4);
        long nodes2 = r2.getNodes();

        assertTrue(nodes2 < nodes1, "Warm transposition table should result in fewer or equal nodes");
        assertEquals(r1.getBestMove(), r2.getBestMove());
    }
}
