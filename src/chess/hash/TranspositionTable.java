package chess.hash;

import chess.model.Move;
import chess.search.Search;

public final class TranspositionTable {
    public static final byte EXACT = 0;
    public static final byte LOWERBOUND = 1;
    public static final byte UPPERBOUND = 2;

    public static final class Entry {
        public long hash;
        public Move move;
        public int score;
        public int depth;
        public byte flag;
        public byte age;

        public int getScore(int ply) {
            if (score >= Search.MATE_SCORE - 100) {
                return score - ply;
            }
            if (score <= -Search.MATE_SCORE + 100) {
                return score + ply;
            }
            return score;
        }
    }

    private final Entry[] table;
    private final int mask;
    private byte currentAge = 0;

    public TranspositionTable() {
        this(1 << 19);
    }

    public TranspositionTable(int size) {
        int capacity = 1;
        while (capacity < size) {
            capacity <<= 1;
        }
        this.table = new Entry[capacity];
        this.mask = capacity - 1;
    }

    public void newSearch() {
        currentAge++;
    }

    public void clear() {
        for (int i = 0; i < table.length; i++) {
            table[i] = null;
        }
        currentAge = 0;
    }

    public Entry probe(long hash) {
        int index = (int) (hash & mask);
        Entry entry = table[index];
        if (entry != null && entry.hash == hash) {
            return entry;
        }
        return null;
    }

    public void store(long hash, int depth, int score, byte flag, Move move, int ply) {
        int index = (int) (hash & mask);
        Entry existing = table[index];

        int storedScore = score;
        if (score >= Search.MATE_SCORE - 100) {
            storedScore = score + ply;
        } else if (score <= -Search.MATE_SCORE + 100) {
            storedScore = score - ply;
        }

        if (existing == null || existing.age != currentAge || depth >= existing.depth) {
            Entry entry = new Entry();
            entry.hash = hash;
            entry.depth = depth;
            entry.score = storedScore;
            entry.flag = flag;
            entry.move = move != null ? move : (existing != null && existing.hash == hash ? existing.move : null);
            entry.age = currentAge;
            table[index] = entry;
        } else if (existing.hash == hash && move != null && existing.move == null) {
            existing.move = move;
        }
    }
}
