package chess.test;

import chess.io.Fen;
import chess.model.Board;
import chess.model.Move;
import chess.movegen.MoveGenerator;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PerftTest {
    private static final String START_POS = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private static final String KIWIPETE = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";
    private static final String POS3 = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1";
    private static final String POS4 = "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1";
    private static final String POS5 = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8";
    private static final String POS6 = "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10";

    public static long perft(Board board, int depth) {
        if (depth == 0) {
            return 1L;
        }
        List<Move> moves = MoveGenerator.generateLegalMoves(board);
        if (depth == 1) {
            return moves.size();
        }
        long nodes = 0L;
        for (Move move : moves) {
            board.makeMove(move);
            nodes += perft(board, depth - 1);
            board.unmakeMove();
        }
        return nodes;
    }

    public static Map<String, Long> divide(Board board, int depth) {
        List<Move> moves = MoveGenerator.generateLegalMoves(board);
        Map<String, Long> counts = new LinkedHashMap<>();
        long total = 0L;
        for (Move move : moves) {
            board.makeMove(move);
            long count = (depth == 1) ? 1L : perft(board, depth - 1);
            board.unmakeMove();
            counts.put(move.toUci(), count);
            total += count;
            System.out.println(move.toUci() + ": " + count);
        }
        System.out.println("Total: " + total);
        return counts;
    }

    @Test
    public void testStartingPosition() {
        Board board = Fen.parse(START_POS);
        assertEquals(20L, perft(board, 1));
        assertEquals(400L, perft(board, 2));
        assertEquals(8902L, perft(board, 3));
        assertEquals(197281L, perft(board, 4));
        assertEquals(4865609L, perft(board, 5));
    }

    @Test
    public void testKiwipete() {
        Board board = Fen.parse(KIWIPETE);
        assertEquals(48L, perft(board, 1));
        assertEquals(2039L, perft(board, 2));
        assertEquals(97862L, perft(board, 3));
        assertEquals(4085603L, perft(board, 4));
    }

    @Test
    public void testPosition3() {
        Board board = Fen.parse(POS3);
        assertEquals(14L, perft(board, 1));
        assertEquals(191L, perft(board, 2));
        assertEquals(2812L, perft(board, 3));
        assertEquals(43238L, perft(board, 4));
        assertEquals(674624L, perft(board, 5));
    }

    @Test
    public void testPosition4() {
        Board board = Fen.parse(POS4);
        assertEquals(6L, perft(board, 1));
        assertEquals(264L, perft(board, 2));
        assertEquals(9467L, perft(board, 3));
        assertEquals(422333L, perft(board, 4));
    }

    @Test
    public void testPosition5() {
        Board board = Fen.parse(POS5);
        assertEquals(44L, perft(board, 1));
        assertEquals(1486L, perft(board, 2));
        assertEquals(62379L, perft(board, 3));
        assertEquals(2103487L, perft(board, 4));
    }

    @Test
    public void testPosition6() {
        Board board = Fen.parse(POS6);
        assertEquals(46L, perft(board, 1));
        assertEquals(2079L, perft(board, 2));
        assertEquals(89890L, perft(board, 3));
        assertEquals(3894594L, perft(board, 4));
    }
}
