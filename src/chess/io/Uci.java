package chess.io;

import chess.hash.RepetitionTracker;
import chess.model.Board;
import chess.model.Move;
import chess.model.PieceColor;
import chess.movegen.MoveGenerator;
import chess.search.MaterialPstEvaluator;
import chess.search.Search;
import chess.search.SearchResult;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

public final class Uci {
    private Board board;
    private final RepetitionTracker repetitionTracker;
    private final Search search;
    private final Scanner scanner;
    private final PrintStream out;
    private boolean running = true;

    public Uci() {
        this(System.in, System.out);
    }

    public Uci(InputStream in, PrintStream out) {
        this.board = Fen.parse(Fen.START_FEN);
        this.repetitionTracker = new RepetitionTracker();
        this.repetitionTracker.addGamePosition(board.getZobristHash());
        this.search = new Search(new MaterialPstEvaluator(), repetitionTracker);
        this.scanner = new Scanner(in);
        this.out = out;
    }

    public static void main(String[] args) {
        Uci uci = new Uci();
        uci.run();
    }

    public Board getBoard() {
        return board;
    }

    public Search getSearch() {
        return search;
    }

    public void run() {
        while (running && scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                processCommand(line);
            }
        }
    }

    public void processCommand(String command) {
        String[] tokens = command.trim().split("\\s+");
        String cmd = tokens[0].toLowerCase();

        switch (cmd) {
            case "uci" -> {
                out.println("id name JavaChessEngine");
                out.println("id author Antigravity");
                out.println("uciok");
                out.flush();
            }
            case "isready" -> {
                out.println("readyok");
                out.flush();
            }
            case "ucinewgame" -> {
                board = Fen.parse(Fen.START_FEN);
                repetitionTracker.clear();
                repetitionTracker.addGamePosition(board.getZobristHash());
                search.getTranspositionTable().clear();
            }
            case "position" -> handlePosition(tokens);
            case "go" -> handleGo(tokens);
            case "quit" -> running = false;
            default -> {}
        }
    }

    private void handlePosition(String[] tokens) {
        if (tokens.length < 2) return;

        int movesIndex = -1;
        for (int i = 0; i < tokens.length; i++) {
            if ("moves".equalsIgnoreCase(tokens[i])) {
                movesIndex = i;
                break;
            }
        }

        if ("startpos".equalsIgnoreCase(tokens[1])) {
            board = Fen.parse(Fen.START_FEN);
            repetitionTracker.clear();
            repetitionTracker.addGamePosition(board.getZobristHash());
        } else if ("fen".equalsIgnoreCase(tokens[1])) {
            int fenEnd = (movesIndex != -1) ? movesIndex : tokens.length;
            StringBuilder fenSb = new StringBuilder();
            for (int i = 2; i < fenEnd; i++) {
                if (i > 2) fenSb.append(' ');
                fenSb.append(tokens[i]);
            }
            board = Fen.parse(fenSb.toString());
            repetitionTracker.clear();
            repetitionTracker.addGamePosition(board.getZobristHash());
        }

        if (movesIndex != -1) {
            for (int i = movesIndex + 1; i < tokens.length; i++) {
                String moveStr = tokens[i];
                Move legal = findLegalMove(moveStr);
                if (legal != null) {
                    board.makeMove(legal);
                    repetitionTracker.addGamePosition(board.getZobristHash());
                }
            }
        }
    }

    private void handleGo(String[] tokens) {
        int depth = 6;
        long timeLimit = 1500;

        long wtime = -1;
        long btime = -1;
        long winc = 0;
        long binc = 0;

        for (int i = 1; i < tokens.length; i++) {
            String token = tokens[i].toLowerCase();
            if ("depth".equals(token) && i + 1 < tokens.length) {
                try {
                    depth = Integer.parseInt(tokens[i + 1]);
                    timeLimit = 0;
                } catch (NumberFormatException ignored) {}
                i++;
            } else if ("movetime".equals(token) && i + 1 < tokens.length) {
                try {
                    timeLimit = Long.parseLong(tokens[i + 1]);
                    depth = 64;
                } catch (NumberFormatException ignored) {}
                i++;
            } else if ("wtime".equals(token) && i + 1 < tokens.length) {
                try { wtime = Long.parseLong(tokens[i + 1]); } catch (NumberFormatException ignored) {}
                i++;
            } else if ("btime".equals(token) && i + 1 < tokens.length) {
                try { btime = Long.parseLong(tokens[i + 1]); } catch (NumberFormatException ignored) {}
                i++;
            } else if ("winc".equals(token) && i + 1 < tokens.length) {
                try { winc = Long.parseLong(tokens[i + 1]); } catch (NumberFormatException ignored) {}
                i++;
            } else if ("binc".equals(token) && i + 1 < tokens.length) {
                try { binc = Long.parseLong(tokens[i + 1]); } catch (NumberFormatException ignored) {}
                i++;
            } else if ("infinite".equals(token)) {
                depth = 64;
                timeLimit = 0;
            }
        }

        long myTime = board.getSideToMove() == PieceColor.WHITE ? wtime : btime;
        long myInc = board.getSideToMove() == PieceColor.WHITE ? winc : binc;
        if (myTime > 0) {
            timeLimit = Math.max(50, (myTime / 25) + (myInc / 2));
            timeLimit = Math.min(timeLimit, myTime / 2);
            depth = 64;
        }

        SearchResult result = search.search(board, depth, timeLimit, 1);
        Move bestMove = result.getBestMove();
        out.println("bestmove " + (bestMove != null ? bestMove.toUci() : "0000"));
        out.flush();
    }

    private Move findLegalMove(String moveStr) {
        List<Move> legalMoves = MoveGenerator.generateLegalMoves(board);
        for (Move move : legalMoves) {
            if (move.toUci().equalsIgnoreCase(moveStr)) {
                return move;
            }
        }
        return null;
    }
}
