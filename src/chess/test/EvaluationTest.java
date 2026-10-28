package chess.test;

import chess.io.Fen;
import chess.model.Board;
import chess.model.Move;
import chess.model.Piece;
import chess.model.PieceColor;
import chess.model.PieceType;
import chess.model.Square;
import chess.search.MaterialPstEvaluator;
import chess.search.MoveOrderer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EvaluationTest {
    private final MaterialPstEvaluator evaluator = new MaterialPstEvaluator();

    @Test
    public void testStartingPositionSymmetry() {
        Board board = Fen.parse(Fen.START_FEN);
        assertEquals(0, evaluator.evaluateAbsolute(board));
        assertEquals(0, evaluator.evaluate(board));
    }

    @Test
    public void testMaterialAdvantage() {
        Board whiteAhead = Fen.parse("rnb1kbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        assertTrue(evaluator.evaluateAbsolute(whiteAhead) > 800);
        assertTrue(evaluator.evaluate(whiteAhead) > 800);

        Board blackAhead = Fen.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNB1KBNR w KQkq - 0 1");
        assertTrue(evaluator.evaluateAbsolute(blackAhead) < -800);
        assertTrue(evaluator.evaluate(blackAhead) < -800);

        Board blackAheadBlackToMove = Fen.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNB1KBNR b KQkq - 0 1");
        assertTrue(evaluator.evaluate(blackAheadBlackToMove) > 800);
    }

    @Test
    public void testPositionalKnightCentralBonus() {
        Board centerKnight = Fen.parse("8/8/8/4N3/8/8/4K3/4k3 w - - 0 1");
        Board cornerKnight = Fen.parse("8/8/8/8/8/8/4K3/N3k3 w - - 0 1");
        assertTrue(evaluator.evaluateAbsolute(centerKnight) > evaluator.evaluateAbsolute(cornerKnight));
    }

    @Test
    public void testMoveOrdering() {
        List<Move> moves = new ArrayList<>();
        Move quietMove = new Move(Square.fromName("e2"), Square.fromName("e4"),
                Piece.of(PieceType.PAWN, PieceColor.WHITE));
        Move pawnTakesQueen = new Move(Square.fromName("e4"), Square.fromName("d5"),
                Piece.of(PieceType.PAWN, PieceColor.WHITE),
                Piece.of(PieceType.QUEEN, PieceColor.BLACK));
        Move queenTakesPawn = new Move(Square.fromName("d1"), Square.fromName("d5"),
                Piece.of(PieceType.QUEEN, PieceColor.WHITE),
                Piece.of(PieceType.PAWN, PieceColor.BLACK));

        moves.add(quietMove);
        moves.add(queenTakesPawn);
        moves.add(pawnTakesQueen);

        MoveOrderer.orderMoves(moves);

        assertEquals(pawnTakesQueen, moves.get(0));
        assertEquals(queenTakesPawn, moves.get(1));
        assertEquals(quietMove, moves.get(2));
    }
}
