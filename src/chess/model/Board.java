package chess.model;

import chess.hash.Zobrist;
import chess.movegen.AttackDetector;

import java.util.ArrayDeque;
import java.util.Deque;

public final class Board {
    private final Piece[] squares = new Piece[64];
    private PieceColor sideToMove = PieceColor.WHITE;
    private boolean whiteKingsideCastle = true;
    private boolean whiteQueensideCastle = true;
    private boolean blackKingsideCastle = true;
    private boolean blackQueensideCastle = true;
    private int enPassantSquare = Square.NO_SQUARE;
    private int halfmoveClock = 0;
    private int fullmoveNumber = 1;
    private long zobristHash = 0L;
    private final Deque<UndoState> history = new ArrayDeque<>();

    private static class UndoState {
        final Move move;
        final boolean whiteKingsideCastle;
        final boolean whiteQueensideCastle;
        final boolean blackKingsideCastle;
        final boolean blackQueensideCastle;
        final int enPassantSquare;
        final int halfmoveClock;
        final long zobristHash;

        UndoState(Move move, boolean wk, boolean wq, boolean bk, boolean bq, int ep, int halfmove, long hash) {
            this.move = move;
            this.whiteKingsideCastle = wk;
            this.whiteQueensideCastle = wq;
            this.blackKingsideCastle = bk;
            this.blackQueensideCastle = bq;
            this.enPassantSquare = ep;
            this.halfmoveClock = halfmove;
            this.zobristHash = hash;
        }
    }

    public Piece getPiece(int square) {
        if (!Square.isValid(square)) {
            return null;
        }
        return squares[square];
    }

    public Piece getPiece(int file, int rank) {
        if (!Square.isValid(file, rank)) {
            return null;
        }
        return squares[Square.of(file, rank)];
    }

    public void setPiece(int square, Piece piece) {
        if (Square.isValid(square)) {
            squares[square] = piece;
        }
    }

    public PieceColor getSideToMove() {
        return sideToMove;
    }

    public void setSideToMove(PieceColor sideToMove) {
        this.sideToMove = sideToMove;
    }

    public boolean canWhiteCastleKingside() {
        return whiteKingsideCastle;
    }

    public void setWhiteCastleKingside(boolean whiteKingsideCastle) {
        this.whiteKingsideCastle = whiteKingsideCastle;
    }

    public boolean canWhiteCastleQueenside() {
        return whiteQueensideCastle;
    }

    public void setWhiteCastleQueenside(boolean whiteQueensideCastle) {
        this.whiteQueensideCastle = whiteQueensideCastle;
    }

    public boolean canBlackCastleKingside() {
        return blackKingsideCastle;
    }

    public void setBlackCastleKingside(boolean blackKingsideCastle) {
        this.blackKingsideCastle = blackKingsideCastle;
    }

    public boolean canBlackCastleQueenside() {
        return blackQueensideCastle;
    }

    public void setBlackCastleQueenside(boolean blackQueensideCastle) {
        this.blackQueensideCastle = blackQueensideCastle;
    }

    public boolean hasCastlingRight(PieceColor color, boolean kingside) {
        if (color == PieceColor.WHITE) {
            return kingside ? whiteKingsideCastle : whiteQueensideCastle;
        }
        return kingside ? blackKingsideCastle : blackQueensideCastle;
    }

    public void setCastlingRight(PieceColor color, boolean kingside, boolean value) {
        if (color == PieceColor.WHITE) {
            if (kingside) whiteKingsideCastle = value;
            else whiteQueensideCastle = value;
        } else {
            if (kingside) blackKingsideCastle = value;
            else blackQueensideCastle = value;
        }
    }

    public int getEnPassantSquare() {
        return enPassantSquare;
    }

    public void setEnPassantSquare(int enPassantSquare) {
        this.enPassantSquare = enPassantSquare;
    }

    public int getHalfmoveClock() {
        return halfmoveClock;
    }

    public void setHalfmoveClock(int halfmoveClock) {
        this.halfmoveClock = halfmoveClock;
    }

    public int getFullmoveNumber() {
        return fullmoveNumber;
    }

    public void setFullmoveNumber(int fullmoveNumber) {
        this.fullmoveNumber = fullmoveNumber;
    }

    public long getZobristHash() {
        return zobristHash;
    }

    public void setZobristHash(long zobristHash) {
        this.zobristHash = zobristHash;
    }

    public void recomputeHash() {
        this.zobristHash = Zobrist.computeHash(this);
    }

    public void clear() {
        for (int i = 0; i < 64; i++) {
            squares[i] = null;
        }
        sideToMove = PieceColor.WHITE;
        whiteKingsideCastle = false;
        whiteQueensideCastle = false;
        blackKingsideCastle = false;
        blackQueensideCastle = false;
        enPassantSquare = Square.NO_SQUARE;
        halfmoveClock = 0;
        fullmoveNumber = 1;
        zobristHash = 0L;
        history.clear();
    }

    public int findKing(PieceColor color) {
        for (int i = 0; i < 64; i++) {
            Piece piece = squares[i];
            if (piece != null && piece.getType() == PieceType.KING && piece.getColor() == color) {
                return i;
            }
        }
        return Square.NO_SQUARE;
    }

    public boolean isInCheck(PieceColor color) {
        int kingSquare = findKing(color);
        if (kingSquare == Square.NO_SQUARE) {
            return false;
        }
        return AttackDetector.isSquareAttacked(this, kingSquare, color.opposite());
    }

    public boolean isSquareAttacked(int square, PieceColor bySide) {
        return AttackDetector.isSquareAttacked(this, square, bySide);
    }

    public void makeMove(Move move) {
        history.push(new UndoState(move, whiteKingsideCastle, whiteQueensideCastle,
                blackKingsideCastle, blackQueensideCastle, enPassantSquare, halfmoveClock, zobristHash));

        int from = move.getFrom();
        int to = move.getTo();
        Piece movedPiece = move.getPieceMoved();

        zobristHash ^= Zobrist.pieceKey(movedPiece, from);
        squares[from] = null;

        if (move.getCapturedPiece() != null) {
            if (move.getFlag() == MoveFlag.EN_PASSANT) {
                int capSquare = Square.of(Square.file(to), Square.rank(from));
                zobristHash ^= Zobrist.pieceKey(move.getCapturedPiece(), capSquare);
                squares[capSquare] = null;
            } else {
                zobristHash ^= Zobrist.pieceKey(move.getCapturedPiece(), to);
            }
        }

        if (move.getFlag() == MoveFlag.PROMOTION) {
            squares[to] = move.getPromotionPiece();
            zobristHash ^= Zobrist.pieceKey(move.getPromotionPiece(), to);
        } else if (move.getFlag() == MoveFlag.CASTLE) {
            squares[to] = movedPiece;
            zobristHash ^= Zobrist.pieceKey(movedPiece, to);

            int rookFrom;
            int rookTo;
            if (to == Square.G1) {
                rookFrom = Square.H1;
                rookTo = Square.F1;
            } else if (to == Square.C1) {
                rookFrom = Square.A1;
                rookTo = Square.D1;
            } else if (to == Square.G8) {
                rookFrom = Square.H8;
                rookTo = Square.F8;
            } else {
                rookFrom = Square.A8;
                rookTo = Square.D8;
            }
            Piece rook = squares[rookFrom];
            squares[rookFrom] = null;
            squares[rookTo] = rook;
            zobristHash ^= Zobrist.pieceKey(rook, rookFrom);
            zobristHash ^= Zobrist.pieceKey(rook, rookTo);
        } else {
            squares[to] = movedPiece;
            zobristHash ^= Zobrist.pieceKey(movedPiece, to);
        }

        boolean oldWK = whiteKingsideCastle;
        boolean oldWQ = whiteQueensideCastle;
        boolean oldBK = blackKingsideCastle;
        boolean oldBQ = blackQueensideCastle;

        if (from == Square.E1) {
            whiteKingsideCastle = false;
            whiteQueensideCastle = false;
        } else if (from == Square.E8) {
            blackKingsideCastle = false;
            blackQueensideCastle = false;
        }
        if (from == Square.H1 || to == Square.H1) {
            whiteKingsideCastle = false;
        }
        if (from == Square.A1 || to == Square.A1) {
            whiteQueensideCastle = false;
        }
        if (from == Square.H8 || to == Square.H8) {
            blackKingsideCastle = false;
        }
        if (from == Square.A8 || to == Square.A8) {
            blackQueensideCastle = false;
        }

        if (oldWK != whiteKingsideCastle) zobristHash ^= Zobrist.castleKey(0);
        if (oldWQ != whiteQueensideCastle) zobristHash ^= Zobrist.castleKey(1);
        if (oldBK != blackKingsideCastle) zobristHash ^= Zobrist.castleKey(2);
        if (oldBQ != blackQueensideCastle) zobristHash ^= Zobrist.castleKey(3);

        if (enPassantSquare != Square.NO_SQUARE) {
            zobristHash ^= Zobrist.enPassantKey(Square.file(enPassantSquare));
        }

        if (movedPiece.getType() == PieceType.PAWN && Math.abs(Square.rank(to) - Square.rank(from)) == 2) {
            enPassantSquare = (from + to) / 2;
            zobristHash ^= Zobrist.enPassantKey(Square.file(enPassantSquare));
        } else {
            enPassantSquare = Square.NO_SQUARE;
        }

        if (movedPiece.getType() == PieceType.PAWN || move.isCapture()) {
            halfmoveClock = 0;
        } else {
            halfmoveClock++;
        }

        sideToMove = sideToMove.opposite();
        zobristHash ^= Zobrist.sideKey();

        if (sideToMove == PieceColor.WHITE) {
            fullmoveNumber++;
        }
    }

    public void unmakeMove() {
        if (history.isEmpty()) {
            return;
        }
        UndoState state = history.pop();
        Move move = state.move;
        int from = move.getFrom();
        int to = move.getTo();
        Piece movedPiece = move.getPieceMoved();

        if (sideToMove == PieceColor.WHITE) {
            fullmoveNumber--;
        }
        sideToMove = sideToMove.opposite();

        squares[from] = movedPiece;

        if (move.getFlag() == MoveFlag.CASTLE) {
            squares[to] = null;
            if (to == Square.G1) {
                squares[Square.H1] = squares[Square.F1];
                squares[Square.F1] = null;
            } else if (to == Square.C1) {
                squares[Square.A1] = squares[Square.D1];
                squares[Square.D1] = null;
            } else if (to == Square.G8) {
                squares[Square.H8] = squares[Square.F8];
                squares[Square.F8] = null;
            } else if (to == Square.C8) {
                squares[Square.A8] = squares[Square.D8];
                squares[Square.D8] = null;
            }
        } else if (move.getFlag() == MoveFlag.EN_PASSANT) {
            squares[to] = null;
            int capSquare = Square.of(Square.file(to), Square.rank(from));
            squares[capSquare] = move.getCapturedPiece();
        } else {
            squares[to] = move.getCapturedPiece();
        }

        whiteKingsideCastle = state.whiteKingsideCastle;
        whiteQueensideCastle = state.whiteQueensideCastle;
        blackKingsideCastle = state.blackKingsideCastle;
        blackQueensideCastle = state.blackQueensideCastle;
        enPassantSquare = state.enPassantSquare;
        halfmoveClock = state.halfmoveClock;
        zobristHash = state.zobristHash;
    }
}
