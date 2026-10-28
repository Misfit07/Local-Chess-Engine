package chess.ui;

import chess.hash.RepetitionTracker;
import chess.io.Fen;
import chess.model.Board;
import chess.model.PieceColor;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public final class ChessFrame extends JFrame {
    private final BoardPanel boardPanel;
    private final JLabel statusLabel;

    public ChessFrame() {
        setTitle("Java Chess Engine");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        this.boardPanel = new BoardPanel();
        this.statusLabel = new JLabel(" White to move | Ready");
        this.statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        this.statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));

        boardPanel.setStatusListener(status -> statusLabel.setText(" " + status));

        add(boardPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        setJMenuBar(createMenuBar());

        startNewGame();

        pack();
        setMinimumSize(new Dimension(520, 580));
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new ChessFrame().setVisible(true);
        });
    }

    public BoardPanel getBoardPanel() {
        return boardPanel;
    }

    private void startNewGame() {
        Board fresh = Fen.parse(Fen.START_FEN);
        RepetitionTracker tracker = new RepetitionTracker();
        tracker.addGamePosition(fresh.getZobristHash());
        boardPanel.setBoard(fresh, tracker);
        if (boardPanel.getHumanSide() == PieceColor.BLACK) {
            boardPanel.setFlipped(true);
            boardPanel.triggerEngineMove();
        } else {
            boardPanel.setFlipped(false);
        }
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGameItem = new JMenuItem("New Game");
        newGameItem.setAccelerator(KeyStroke.getKeyStroke("control N"));
        newGameItem.addActionListener(e -> startNewGame());

        JMenuItem flipItem = new JMenuItem("Flip Board");
        flipItem.setAccelerator(KeyStroke.getKeyStroke("control F"));
        flipItem.addActionListener(e -> boardPanel.setFlipped(!boardPanel.isFlipped()));

        JMenuItem undoItem = new JMenuItem("Undo Move");
        undoItem.setAccelerator(KeyStroke.getKeyStroke("control Z"));
        undoItem.addActionListener(e -> boardPanel.undoMove());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        gameMenu.add(newGameItem);
        gameMenu.add(flipItem);
        gameMenu.add(undoItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        JMenu modeMenu = new JMenu("Mode");
        ButtonGroup modeGroup = new ButtonGroup();

        JRadioButtonMenuItem whiteItem = new JRadioButtonMenuItem("Play as White", true);
        whiteItem.addActionListener(e -> {
            boardPanel.setHumanSide(PieceColor.WHITE);
            boardPanel.setFlipped(false);
            startNewGame();
        });

        JRadioButtonMenuItem blackItem = new JRadioButtonMenuItem("Play as Black");
        blackItem.addActionListener(e -> {
            boardPanel.setHumanSide(PieceColor.BLACK);
            boardPanel.setFlipped(true);
            startNewGame();
        });

        JRadioButtonMenuItem twoPlayerItem = new JRadioButtonMenuItem("Human vs Human (Two Players)");
        twoPlayerItem.addActionListener(e -> {
            boardPanel.setHumanSide(null);
            boardPanel.setFlipped(false);
            startNewGame();
        });

        modeGroup.add(whiteItem);
        modeGroup.add(blackItem);
        modeGroup.add(twoPlayerItem);

        modeMenu.add(whiteItem);
        modeMenu.add(blackItem);
        modeMenu.add(twoPlayerItem);

        JMenu diffMenu = new JMenu("Difficulty");
        ButtonGroup diffGroup = new ButtonGroup();

        JRadioButtonMenuItem d1 = new JRadioButtonMenuItem("Casual (Depth 2, 200ms)");
        d1.addActionListener(e -> {
            boardPanel.setEngineDepth(2);
            boardPanel.setEngineTimeMs(200);
        });

        JRadioButtonMenuItem d2 = new JRadioButtonMenuItem("Easy (Depth 4, 600ms)");
        d2.addActionListener(e -> {
            boardPanel.setEngineDepth(4);
            boardPanel.setEngineTimeMs(600);
        });

        JRadioButtonMenuItem d3 = new JRadioButtonMenuItem("Medium (Depth 6, 1500ms)", true);
        d3.addActionListener(e -> {
            boardPanel.setEngineDepth(6);
            boardPanel.setEngineTimeMs(1500);
        });

        JRadioButtonMenuItem d4 = new JRadioButtonMenuItem("Hard (Depth 8, 3000ms)");
        d4.addActionListener(e -> {
            boardPanel.setEngineDepth(8);
            boardPanel.setEngineTimeMs(3000);
        });

        diffGroup.add(d1);
        diffGroup.add(d2);
        diffGroup.add(d3);
        diffGroup.add(d4);

        diffMenu.add(d1);
        diffMenu.add(d2);
        diffMenu.add(d3);
        diffMenu.add(d4);

        JMenu fenMenu = new JMenu("FEN");
        JMenuItem copyFenItem = new JMenuItem("Copy FEN");
        copyFenItem.addActionListener(e -> {
            String fen = Fen.export(boardPanel.getBoard());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(fen), null);
            JOptionPane.showMessageDialog(this, "FEN copied to clipboard:\n" + fen, "FEN Export", JOptionPane.INFORMATION_MESSAGE);
        });

        JMenuItem loadFenItem = new JMenuItem("Load FEN...");
        loadFenItem.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter FEN string:", "Load FEN", JOptionPane.PLAIN_MESSAGE);
            if (input != null && !input.trim().isEmpty()) {
                try {
                    Board loaded = Fen.parse(input.trim());
                    RepetitionTracker tracker = new RepetitionTracker();
                    tracker.addGamePosition(loaded.getZobristHash());
                    boardPanel.setBoard(loaded, tracker);
                    boardPanel.triggerEngineMove();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid FEN: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        fenMenu.add(copyFenItem);
        fenMenu.add(loadFenItem);

        menuBar.add(gameMenu);
        menuBar.add(modeMenu);
        menuBar.add(diffMenu);
        menuBar.add(fenMenu);

        return menuBar;
    }
}
