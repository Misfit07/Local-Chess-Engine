package chess.test;

import chess.hash.Zobrist;
import chess.io.Fen;
import chess.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {

    @Test
    public void testSquareCoordinates() {
        assertEquals(Square.A1, Square.of(0, 0));
        assertEquals(Square.H1, Square.of(7, 0));
        assertEquals(Square.A8, Square.of(0, 7));
        assertEquals(Square.H8, Square.of(7, 7));
        assertEquals("e4", Square.name(Square.of(4, 3)));
        assertEquals(Square.of(4, 3), Square.fromName("e4"));
    }

    @Test
    public void testMakeUnmakeQuietMoveRestoresStateAndHash() {
        Board board = Fen.parse(Fen.START_FEN);
        String initialFen = Fen.export(board);
        long initialHash = board.getZobristHash();

        Move move = new Move(Square.fromName("e2"), Square.fromName("e4"),
                board.getPiece(Square.fromName("e2")));
        board.makeMove(move);

        assertEquals(PieceColor.BLACK, board.getSideToMove());
        assertEquals(Square.fromName("e3"), board.getEnPassantSquare());
        assertEquals(Zobrist.computeHash(board), board.getZobristHash());

        board.unmakeMove();

        assertEquals(initialFen, Fen.export(board));
        assertEquals(initialHash, board.getZobristHash());
        assertEquals(Zobrist.computeHash(board), board.getZobristHash());
    }

    @Test
    public void testIncrementalHashMatchesComputeHashOverMoveSequence() {
        Board board = Fen.parse(Fen.START_FEN);
        Move[] sequence = {
                new Move(Square.fromName("e2"), Square.fromName("e4"), board.getPiece(Square.fromName("e2"))),
                new Move(Square.fromName("e7"), Square.fromName("e5"), Piece.of(PieceType.PAWN, PieceColor.BLACK)),
                new Move(Square.fromName("g1"), Square.fromName("f3"), Piece.of(PieceType.KNIGHT, PieceColor.WHITE)),
                new Move(Square.fromName("b8"), Square.fromName("c6"), Piece.of(PieceType.KNIGHT, PieceColor.BLACK)),
                new Move(Square.fromName("f1"), Square.fromName("c4"), Piece.of(PieceType.BISHOP, PieceColor.WHITE)),
                new Move(Square.fromName("g8"), Square.fromName("f6"), Piece.of(PieceType.KNIGHT, PieceColor.BLACK))
        };

        for (Move move : sequence) {
            board.makeMove(move);
            assertEquals(Zobrist.computeHash(board), board.getZobristHash());
        }

        for (int i = sequence.length - 1; i >= 0; i--) {
            board.unmakeMove();
            assertEquals(Zobrist.computeHash(board), board.getZobristHash());
        }

        assertEquals(Fen.START_FEN, Fen.export(board));
    }

    @Test
    public void testCastlingExecutionAndUndo() {
        String fen = "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1";
        Board board = Fen.parse(fen);
        long initialHash = board.getZobristHash();

        Move castleMove = new Move(Square.E1, Square.G1, board.getPiece(Square.E1), null, null, MoveFlag.CASTLE);
        board.makeMove(castleMove);

        assertEquals(Piece.of(PieceType.KING, PieceColor.WHITE), board.getPiece(Square.G1));
        assertEquals(Piece.of(PieceType.ROOK, PieceColor.WHITE), board.getPiece(Square.F1));
        assertNull(board.getPiece(Square.E1));
        assertNull(board.getPiece(Square.H1));
        assertFalse(board.canWhiteCastleKingside());
        assertFalse(board.canWhiteCastleQueenside());
        assertEquals(Zobrist.computeHash(board), board.getZobristHash());

        board.unmakeMove();

        assertEquals(fen, Fen.export(board));
        assertEquals(initialHash, board.getZobristHash());
    }

    @Test
    public void testEnPassantExecutionAndUndo() {
        String fen = "rnbqkbnr/ppp1p1pp/8/3pPp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3";
        Board board = Fen.parse(fen);
        long initialHash = board.getZobristHash();

        Move epMove = new Move(Square.fromName("e5"), Square.fromName("f6"),
                board.getPiece(Square.fromName("e5")),
                board.getPiece(Square.fromName("f5")),
                null,
                MoveFlag.EN_PASSANT);

        board.makeMove(epMove);

        assertEquals(Piece.of(PieceType.PAWN, PieceColor.WHITE), board.getPiece(Square.fromName("f6")));
        assertNull(board.getPiece(Square.fromName("e5")));
        assertNull(board.getPiece(Square.fromName("f5")));
        assertEquals(Zobrist.computeHash(board), board.getZobristHash());

        board.unmakeMove();

        assertEquals(fen, Fen.export(board));
        assertEquals(initialHash, board.getZobristHash());
    }

    @Test
    public void testPromotionExecutionAndUndo() {
        String fen = "8/4P3/8/8/8/8/8/4K2k w - - 0 1";
        Board board = Fen.parse(fen);
        long initialHash = board.getZobristHash();

        Move promoMove = new Move(Square.fromName("e7"), Square.fromName("e8"),
                board.getPiece(Square.fromName("e7")),
                null,
                Piece.of(PieceType.QUEEN, PieceColor.WHITE),
                MoveFlag.PROMOTION);

        board.makeMove(promoMove);

        assertEquals(Piece.of(PieceType.QUEEN, PieceColor.WHITE), board.getPiece(Square.fromName("e8")));
        assertNull(board.getPiece(Square.fromName("e7")));
        assertEquals(Zobrist.computeHash(board), board.getZobristHash());

        board.unmakeMove();

        assertEquals(fen, Fen.export(board));
        assertEquals(initialHash, board.getZobristHash());
    }
}
