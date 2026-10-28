package chess.movegen;

import chess.model.Board;
import chess.model.Move;
import chess.model.Piece;
import chess.model.PieceColor;

import java.util.ArrayList;
import java.util.List;

public final class MoveGenerator {
    private MoveGenerator() {}

    public static List<Move> generatePseudoLegalMoves(Board board) {
        List<Move> moves = new ArrayList<>(64);
        PieceColor side = board.getSideToMove();
        for (int sq = 0; sq < 64; sq++) {
            Piece piece = board.getPiece(sq);
            if (piece != null && piece.getColor() == side) {
                piece.generatePseudoLegalMoves(board, sq, moves);
            }
        }
        return moves;
    }

    public static List<Move> generateLegalMoves(Board board) {
        List<Move> pseudoLegal = generatePseudoLegalMoves(board);
        List<Move> legalMoves = new ArrayList<>(pseudoLegal.size());
        PieceColor side = board.getSideToMove();

        for (Move move : pseudoLegal) {
            board.makeMove(move);
            if (!board.isInCheck(side)) {
                legalMoves.add(move);
            }
            board.unmakeMove();
        }
        return legalMoves;
    }
}
