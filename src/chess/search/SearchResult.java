package chess.search;

import chess.model.Move;

import java.util.Collections;
import java.util.List;

public final class SearchResult {
    private final Move bestMove;
    private final int score;
    private final int depth;
    private final long nodes;
    private final long timeMs;
    private final List<RootMoveScore> topMoves;

    public SearchResult(Move bestMove, int score, int depth, long nodes, long timeMs, List<RootMoveScore> topMoves) {
        this.bestMove = bestMove;
        this.score = score;
        this.depth = depth;
        this.nodes = nodes;
        this.timeMs = timeMs;
        this.topMoves = topMoves != null ? Collections.unmodifiableList(topMoves) : Collections.emptyList();
    }

    public Move getBestMove() {
        return bestMove;
    }

    public int getScore() {
        return score;
    }

    public int getDepth() {
        return depth;
    }

    public long getNodes() {
        return nodes;
    }

    public long getTimeMs() {
        return timeMs;
    }

    public List<RootMoveScore> getTopMoves() {
        return topMoves;
    }
}
