package chess.search;

import chess.model.Move;

import java.util.List;

public final class MoveOrderer {
    private MoveOrderer() {}

    public static int scoreMove(Move move) {
        if (move.isCapture()) {
            int victimValue = move.getCapturedPiece().getType().getValue();
            int attackerValue = move.getPieceMoved().getType().getValue();
            return 10000 + victimValue * 10 - attackerValue;
        }
        if (move.isPromotion()) {
            return 9000 + move.getPromotionPiece().getType().getValue();
        }
        if (move.isCastle()) {
            return 100;
        }
        return 0;
    }

    public static void orderMoves(List<Move> moves) {
        moves.sort((m1, m2) -> Integer.compare(scoreMove(m2), scoreMove(m1)));
    }
}
