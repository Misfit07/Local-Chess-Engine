package chess.search;

import chess.model.Board;

public interface Evaluator {
    int evaluate(Board board);
    int evaluateAbsolute(Board board);
}
