package chess.ui;

import chess.hash.RepetitionTracker;
import chess.model.*;
import chess.movegen.MoveGenerator;
import chess.search.MaterialPstEvaluator;
import chess.search.Search;
import chess.search.SearchResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public final class BoardPanel extends JPanel {
    public interface StatusListener {
        void onStatusChanged(String status);
    }

    private static final Color LIGHT_SQUARE = new Color(240, 217, 181);
    private static final Color DARK_SQUARE = new Color(181, 136, 99);
    private static final Color SELECTED_COLOR = new Color(186, 202, 68, 200);
    private static final Color LAST_MOVE_COLOR = new Color(205, 210, 106, 130);
    private static final Color CHECK_COLOR = new Color(235, 75, 75, 180);
    private static final Color MOVE_DOT_COLOR = new Color(70, 70, 70, 140);
    private static final Color CAPTURE_RING_COLOR = new Color(70, 70, 70, 160);

    private Board board;
    private RepetitionTracker repetitionTracker;
    private Search search;
    private MaterialPstEvaluator evaluator;

    private int selectedSquare = Square.NO_SQUARE;
    private final List<Move> legalMovesForSelected = new ArrayList<>();
    private Move lastMove = null;
    private boolean flipped = false;
    private PieceColor humanSide = PieceColor.WHITE;
    private int engineDepth = 5;
    private long engineTimeMs = 1500;
    private boolean engineThinking = false;
    private StatusListener statusListener;

    public BoardPanel() {
        this.board = new Board();
        this.repetitionTracker = new RepetitionTracker();
        this.search = new Search(new MaterialPstEvaluator(), repetitionTracker);
        this.evaluator = new MaterialPstEvaluator();

        setPreferredSize(new Dimension(560, 560));
        setBackground(Color.BLACK);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });
    }

    public void setBoard(Board board, RepetitionTracker tracker) {
        this.board = board;
        this.repetitionTracker = tracker;
        this.selectedSquare = Square.NO_SQUARE;
        this.legalMovesForSelected.clear();
        this.lastMove = null;
        repaint();
        updateGameStatus(null);
    }

    public void setStatusListener(StatusListener listener) {
        this.statusListener = listener;
    }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
        repaint();
    }

    public boolean isFlipped() {
        return flipped;
    }

    public void setHumanSide(PieceColor humanSide) {
        this.humanSide = humanSide;
        triggerEngineMove();
    }

    public PieceColor getHumanSide() {
        return humanSide;
    }

    public void setEngineDepth(int engineDepth) {
        this.engineDepth = engineDepth;
    }

    public void setEngineTimeMs(long engineTimeMs) {
        this.engineTimeMs = engineTimeMs;
    }

    public Board getBoard() {
        return board;
    }

    public RepetitionTracker getRepetitionTracker() {
        return repetitionTracker;
    }

    public void undoMove() {
        if (engineThinking) return;
        board.unmakeMove();
        repetitionTracker.removeLastGamePosition();
        if (humanSide != null && board.getSideToMove() != humanSide) {
            board.unmakeMove();
            repetitionTracker.removeLastGamePosition();
        }
        selectedSquare = Square.NO_SQUARE;
        legalMovesForSelected.clear();
        lastMove = null;
        repaint();
        updateGameStatus(null);
    }

    private void handleMouseClick(int px, int py) {
        if (engineThinking) return;
        GameState state = GameStateDetector.getGameState(board, repetitionTracker);
        if (state != GameState.ACTIVE) return;

        if (humanSide != null && board.getSideToMove() != humanSide) {
            return;
        }

        int squareSize = getSquareSize();
        int col = px / squareSize;
        int row = py / squareSize;
        if (col < 0 || col >= 8 || row < 0 || row >= 8) return;

        int clickedSquare = getSquareFromColRow(col, row);

        if (selectedSquare != Square.NO_SQUARE) {
            Move chosenMove = findLegalMove(selectedSquare, clickedSquare);
            if (chosenMove != null) {
                if (chosenMove.isPromotion()) {
                    PieceType promoType = promptPromotionPiece();
                    for (Move m : legalMovesForSelected) {
                        if (m.getTo() == clickedSquare && m.getPromotionPiece().getType() == promoType) {
                            chosenMove = m;
                            break;
                        }
                    }
                }
                board.makeMove(chosenMove);
                repetitionTracker.addGamePosition(board.getZobristHash());
                lastMove = chosenMove;
                selectedSquare = Square.NO_SQUARE;
                legalMovesForSelected.clear();
                repaint();
                updateGameStatus(null);
                triggerEngineMove();
                return;
            }
        }

        Piece clickedPiece = board.getPiece(clickedSquare);
        if (clickedPiece != null && clickedPiece.getColor() == board.getSideToMove()) {
            selectedSquare = clickedSquare;
            legalMovesForSelected.clear();
            List<Move> allLegal = MoveGenerator.generateLegalMoves(board);
            for (Move m : allLegal) {
                if (m.getFrom() == selectedSquare) {
                    legalMovesForSelected.add(m);
                }
            }
        } else {
            selectedSquare = Square.NO_SQUARE;
            legalMovesForSelected.clear();
        }
        repaint();
    }

    private Move findLegalMove(int from, int to) {
        for (Move move : legalMovesForSelected) {
            if (move.getFrom() == from && move.getTo() == to) {
                return move;
            }
        }
        return null;
    }

    private PieceType promptPromotionPiece() {
        String[] options = {"Queen", "Rook", "Bishop", "Knight"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Choose promotion piece:",
                "Pawn Promotion",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );
        return switch (choice) {
            case 1 -> PieceType.ROOK;
            case 2 -> PieceType.BISHOP;
            case 3 -> PieceType.KNIGHT;
            default -> PieceType.QUEEN;
        };
    }

    public void triggerEngineMove() {
        if (engineThinking) return;
        GameState state = GameStateDetector.getGameState(board, repetitionTracker);
        if (state != GameState.ACTIVE) return;

        if (humanSide != null && board.getSideToMove() != humanSide) {
            engineThinking = true;
            if (statusListener != null) {
                statusListener.onStatusChanged("Engine is calculating move...");
            }

            SwingWorker<SearchResult, Void> worker = new SwingWorker<>() {
                @Override
                protected SearchResult doInBackground() {
                    return search.search(board, engineDepth, engineTimeMs, 3);
                }

                @Override
                protected void done() {
                    try {
                        SearchResult result = get();
                        engineThinking = false;
                        Move bestMove = result.getBestMove();
                        if (bestMove != null) {
                            board.makeMove(bestMove);
                            repetitionTracker.addGamePosition(board.getZobristHash());
                            lastMove = bestMove;
                            selectedSquare = Square.NO_SQUARE;
                            legalMovesForSelected.clear();
                            repaint();
                        }
                        updateGameStatus(result);
                    } catch (Exception e) {
                        engineThinking = false;
                        e.printStackTrace();
                    }
                }
            };
            worker.execute();
        }
    }

    private void updateGameStatus(SearchResult result) {
        if (statusListener == null) return;
        GameState state = GameStateDetector.getGameState(board, repetitionTracker);
        if (state != GameState.ACTIVE) {
            switch (state) {
                case CHECKMATE -> statusListener.onStatusChanged("Checkmate! " + board.getSideToMove().opposite() + " wins!");
                case STALEMATE -> statusListener.onStatusChanged("Draw by Stalemate.");
                case DRAW_FIFTY_MOVES -> statusListener.onStatusChanged("Draw by Fifty-Move Rule.");
                case DRAW_INSUFFICIENT_MATERIAL -> statusListener.onStatusChanged("Draw by Insufficient Material.");
                case DRAW_THREEFOLD_REPETITION -> statusListener.onStatusChanged("Draw by Threefold Repetition.");
                default -> {}
            }
            return;
        }

        String side = board.getSideToMove() == PieceColor.WHITE ? "White" : "Black";
        String checkStr = board.isInCheck(board.getSideToMove()) ? " (Check!)" : "";
        int eval = evaluator.evaluateAbsolute(board);
        String evalStr = String.format(" | Eval: %+.2f", eval / 100.0);

        if (result != null && result.getBestMove() != null) {
            evalStr += String.format(" | Engine: %s (%d ms, d=%d)", result.getBestMove().toUci(), result.getTimeMs(), result.getDepth());
        }
        statusListener.onStatusChanged(side + " to move" + checkStr + evalStr);
    }

    private int getSquareSize() {
        return Math.min(getWidth(), getHeight()) / 8;
    }

    private int getSquareFromColRow(int col, int row) {
        int file = flipped ? 7 - col : col;
        int rank = flipped ? row : 7 - row;
        return Square.of(file, rank);
    }

    private Point getColRowFromSquare(int square) {
        int file = Square.file(square);
        int rank = Square.rank(square);
        int col = flipped ? 7 - file : file;
        int row = flipped ? rank : 7 - rank;
        return new Point(col, row);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int sqSize = getSquareSize();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int sq = getSquareFromColRow(col, row);
                boolean isLight = ((Square.file(sq) + Square.rank(sq)) & 1) != 0;
                g2.setColor(isLight ? LIGHT_SQUARE : DARK_SQUARE);
                g2.fillRect(col * sqSize, row * sqSize, sqSize, sqSize);
            }
        }

        if (lastMove != null) {
            Point pFrom = getColRowFromSquare(lastMove.getFrom());
            Point pTo = getColRowFromSquare(lastMove.getTo());
            g2.setColor(LAST_MOVE_COLOR);
            g2.fillRect(pFrom.x * sqSize, pFrom.y * sqSize, sqSize, sqSize);
            g2.fillRect(pTo.x * sqSize, pTo.y * sqSize, sqSize, sqSize);
        }

        if (selectedSquare != Square.NO_SQUARE) {
            Point p = getColRowFromSquare(selectedSquare);
            g2.setColor(SELECTED_COLOR);
            g2.fillRect(p.x * sqSize, p.y * sqSize, sqSize, sqSize);
        }

        if (board.isInCheck(board.getSideToMove())) {
            int kingSq = board.findKing(board.getSideToMove());
            if (kingSq != Square.NO_SQUARE) {
                Point p = getColRowFromSquare(kingSq);
                g2.setColor(CHECK_COLOR);
                g2.fillRect(p.x * sqSize, p.y * sqSize, sqSize, sqSize);
            }
        }

        Font font = new Font(Font.SERIF, Font.PLAIN, (int) (sqSize * 0.78));
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int sq = getSquareFromColRow(col, row);
                Piece piece = board.getPiece(sq);
                if (piece != null) {
                    String symbol = getPieceUnicode(piece);
                    int textWidth = fm.stringWidth(symbol);
                    int textHeight = fm.getAscent();
                    int x = col * sqSize + (sqSize - textWidth) / 2;
                    int y = row * sqSize + (sqSize + textHeight) / 2 - 4;

                    g2.setColor(piece.isWhite() ? Color.WHITE : Color.BLACK);
                    g2.drawString(symbol, x, y);
                    g2.setColor(piece.isWhite() ? new Color(50, 50, 50) : Color.BLACK);
                    g2.drawString(symbol, x, y);
                }
            }
        }

        for (Move move : legalMovesForSelected) {
            Point p = getColRowFromSquare(move.getTo());
            int cx = p.x * sqSize + sqSize / 2;
            int cy = p.y * sqSize + sqSize / 2;
            if (move.isCapture()) {
                g2.setColor(CAPTURE_RING_COLOR);
                g2.setStroke(new BasicStroke((float) (sqSize * 0.08)));
                int r = (int) (sqSize * 0.42);
                g2.drawOval(cx - r, cy - r, r * 2, r * 2);
            } else {
                g2.setColor(MOVE_DOT_COLOR);
                int r = (int) (sqSize * 0.15);
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);
            }
        }

        Font coordFont = new Font(Font.SANS_SERIF, Font.BOLD, (int) (sqSize * 0.18));
        g2.setFont(coordFont);
        for (int i = 0; i < 8; i++) {
            int rank = flipped ? i + 1 : 8 - i;
            int file = flipped ? 7 - i : i;
            boolean rankLight = ((file + rank) & 1) != 0;
            g2.setColor(rankLight ? DARK_SQUARE : LIGHT_SQUARE);
            g2.drawString(String.valueOf(rank), 3, i * sqSize + (int) (sqSize * 0.22));
            char fChar = (char) ('a' + file);
            g2.drawString(String.valueOf(fChar), (i + 1) * sqSize - (int) (sqSize * 0.22), 8 * sqSize - 4);
        }
    }

    private String getPieceUnicode(Piece piece) {
        return switch (piece.getType()) {
            case KING -> piece.isWhite() ? "\u2654" : "\u265A";
            case QUEEN -> piece.isWhite() ? "\u2655" : "\u265B";
            case ROOK -> piece.isWhite() ? "\u2656" : "\u265C";
            case BISHOP -> piece.isWhite() ? "\u2657" : "\u265D";
            case KNIGHT -> piece.isWhite() ? "\u2658" : "\u265E";
            case PAWN -> piece.isWhite() ? "\u2659" : "\u265F";
        };
    }
}
