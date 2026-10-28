package chess.test;

public final class TestRunner {
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

        System.out.println("Stage 1 Tests: " + passed + " passed, " + failed + " failed.");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
