package chess.search;

import chess.model.Move;

public final class RootMoveScore implements Comparable<RootMoveScore> {
    private final Move move;
    private final int score;

    public RootMoveScore(Move move, int score) {
        this.move = move;
        this.score = score;
    }

    public Move getMove() {
        return move;
    }

    public int getScore() {
        return score;
    }

    @Override
    public int compareTo(RootMoveScore other) {
        return Integer.compare(other.score, this.score);
    }

    @Override
    public String toString() {
        return move.toUci() + ": " + score;
    }
}
