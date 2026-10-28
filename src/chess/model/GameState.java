package chess.model;

public enum GameState {
    ACTIVE,
    CHECKMATE,
    STALEMATE,
    DRAW_FIFTY_MOVES,
    DRAW_INSUFFICIENT_MATERIAL,
    DRAW_THREEFOLD_REPETITION
}
