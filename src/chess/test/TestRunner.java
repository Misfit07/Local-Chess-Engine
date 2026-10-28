package chess.test;

import chess.io.Fen;
import chess.model.Board;

public final class TestRunner {
    private static final String START_POS = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private static final String KIWIPETE = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";
    private static final String POS3 = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1";
    private static final String POS4 = "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1";
    private static final String POS5 = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8";
    private static final String POS6 = "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10";

    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;

        FenTest fenTest = new FenTest();
        try {
            fenTest.testStartingPositionRoundTrip();
            passed++;
        } catch (Throwable t) {
            failed++;
            t.printStackTrace();
        }

        try {
            fenTest.testKiwipeteRoundTrip();
            passed++;
        } catch (Throwable t) {
            failed++;
            t.printStackTrace();
        }

        try {
            fenTest.testStandardPositionsRoundTrip();
            passed++;
        } catch (Throwable t) {
            failed++;
            t.printStackTrace();
        }

        try {
            fenTest.testEnPassantTargetParsed();
            passed++;
        } catch (Throwable t) {
            failed++;
            t.printStackTrace();
        }

        BoardTest boardTest = new BoardTest();
        try {
            boardTest.testSquareCoordinates();
            passed++;
        } catch (Throwable t) {
            failed++;
            t.printStackTrace();
        }

        try {
            boardTest.testMakeUnmakeQuietMoveRestoresStateAndHash();
            passed++;
        } catch (Throwable t) {
            failed++;
            t.printStackTrace();
        }

        try {
            boardTest.testIncrementalHashMatchesComputeHashOverMoveSequence();
            passed++;
        } catch (Throwable t) {
            failed++;
            t.printStackTrace();
        }

        try {
            boardTest.testCastlingExecutionAndUndo();
            passed++;
        } catch (Throwable t) {
            failed++;
            t.printStackTrace();
        }

        try {
            boardTest.testEnPassantExecutionAndUndo();
            passed++;
        } catch (Throwable t) {
            failed++;
            t.printStackTrace();
        }

        try {
            boardTest.testPromotionExecutionAndUndo();
            passed++;
        } catch (Throwable t) {
            failed++;
            t.printStackTrace();
        }

        GameStateTest gameStateTest = new GameStateTest();
        try {
            gameStateTest.testCheckDetection();
            passed++;
        } catch (Throwable t) {
            failed++;
            t.printStackTrace();
        }

        try {
            gameStateTest.testFoolsMateCheckmate();
            passed++;
        } catch (Throwable t) {
            failed++;
            t.printStackTrace();
        }

        try {
            gameStateTest.testScholarsMateCheckmate();
            passed++;
        } catch (Throwable t) {
            failed++;
            t.printStackTrace();
        }

        try {
            gameStateTest.testStalemate();
            passed++;
        } catch (Throwable t) {
            failed++;
            t.printStackTrace();
        }

        try {
            gameStateTest.testFiftyMoveRule();
            passed++;
        } catch (Throwable t) {
            failed++;
            t.printStackTrace();
        }

        try {
            gameStateTest.testInsufficientMaterial();
            passed++;
        } catch (Throwable t) {
            failed++;
            t.printStackTrace();
        }

        try {
            gameStateTest.testThreefoldRepetition();
            passed++;
        } catch (Throwable t) {
            failed++;
            t.printStackTrace();
        }

        System.out.println("Model, Board and GameState Tests: " + passed + " passed, " + failed + " failed.");

        System.out.println("\nRunning Perft Tests...");
        int perftPassed = 0;

        perftPassed += checkPerft("StartPos", START_POS, 1, 20L);
        perftPassed += checkPerft("StartPos", START_POS, 2, 400L);
        perftPassed += checkPerft("StartPos", START_POS, 3, 8902L);
        perftPassed += checkPerft("StartPos", START_POS, 4, 197281L);
        perftPassed += checkPerft("StartPos", START_POS, 5, 4865609L);

        perftPassed += checkPerft("Kiwipete", KIWIPETE, 1, 48L);
        perftPassed += checkPerft("Kiwipete", KIWIPETE, 2, 2039L);
        perftPassed += checkPerft("Kiwipete", KIWIPETE, 3, 97862L);
        perftPassed += checkPerft("Kiwipete", KIWIPETE, 4, 4085603L);

        perftPassed += checkPerft("Pos3", POS3, 1, 14L);
        perftPassed += checkPerft("Pos3", POS3, 2, 191L);
        perftPassed += checkPerft("Pos3", POS3, 3, 2812L);
        perftPassed += checkPerft("Pos3", POS3, 4, 43238L);
        perftPassed += checkPerft("Pos3", POS3, 5, 674624L);

        perftPassed += checkPerft("Pos4", POS4, 1, 6L);
        perftPassed += checkPerft("Pos4", POS4, 2, 264L);
        perftPassed += checkPerft("Pos4", POS4, 3, 9467L);
        perftPassed += checkPerft("Pos4", POS4, 4, 422333L);

        perftPassed += checkPerft("Pos5", POS5, 1, 44L);
        perftPassed += checkPerft("Pos5", POS5, 2, 1486L);
        perftPassed += checkPerft("Pos5", POS5, 3, 62379L);
        perftPassed += checkPerft("Pos5", POS5, 4, 2103487L);

        perftPassed += checkPerft("Pos6", POS6, 1, 46L);
        perftPassed += checkPerft("Pos6", POS6, 2, 2079L);
        perftPassed += checkPerft("Pos6", POS6, 3, 89890L);
        perftPassed += checkPerft("Pos6", POS6, 4, 3894594L);

        System.out.println("\nAll Perft Tests: " + perftPassed + " passed.");
    }

    private static int checkPerft(String name, String fen, int depth, long expected) {
        Board board = Fen.parse(fen);
        long start = System.currentTimeMillis();
        long actual = PerftTest.perft(board, depth);
        long elapsed = System.currentTimeMillis() - start;
        if (actual == expected) {
            System.out.println(String.format("  PASS: %s (depth %d) -> %d nodes in %d ms", name, depth, actual, elapsed));
            return 1;
        } else {
            System.err.println(String.format("  FAIL: %s (depth %d) -> expected %d, got %d", name, depth, expected, actual));
            PerftTest.divide(board, depth);
            throw new AssertionError(name + " depth " + depth + " mismatch: expected " + expected + ", got " + actual);
        }
    }
}
