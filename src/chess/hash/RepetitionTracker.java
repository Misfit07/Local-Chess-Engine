package chess.hash;

import java.util.*;

public final class RepetitionTracker {
    private final List<Long> gameHistory = new ArrayList<>();
    private final Map<Long, Integer> gameCounts = new HashMap<>();
    private final Deque<Long> searchStack = new ArrayDeque<>();
    private final Map<Long, Integer> searchCounts = new HashMap<>();

    public void addGamePosition(long hash) {
        gameHistory.add(hash);
        gameCounts.put(hash, gameCounts.getOrDefault(hash, 0) + 1);
    }

    public void removeLastGamePosition() {
        if (!gameHistory.isEmpty()) {
            long hash = gameHistory.remove(gameHistory.size() - 1);
            int count = gameCounts.getOrDefault(hash, 0);
            if (count <= 1) {
                gameCounts.remove(hash);
            } else {
                gameCounts.put(hash, count - 1);
            }
        }
    }

    public void pushSearchPosition(long hash) {
        searchStack.push(hash);
        searchCounts.put(hash, searchCounts.getOrDefault(hash, 0) + 1);
    }

    public void popSearchPosition() {
        if (!searchStack.isEmpty()) {
            long hash = searchStack.pop();
            int count = searchCounts.getOrDefault(hash, 0);
            if (count <= 1) {
                searchCounts.remove(hash);
            } else {
                searchCounts.put(hash, count - 1);
            }
        }
    }

    public int getRepetitionCount(long hash) {
        return gameCounts.getOrDefault(hash, 0) + searchCounts.getOrDefault(hash, 0);
    }

    public boolean isThreefoldRepetition(long hash) {
        return getRepetitionCount(hash) >= 3;
    }

    public boolean isSearchDraw(long hash) {
        return getRepetitionCount(hash) >= 2;
    }

    public void clear() {
        gameHistory.clear();
        gameCounts.clear();
        searchStack.clear();
        searchCounts.clear();
    }

    public void clearSearch() {
        searchStack.clear();
        searchCounts.clear();
    }

    public List<Long> getGameHistory() {
        return Collections.unmodifiableList(gameHistory);
    }
}
