import os
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import parse_xml, OxmlElement
from docx.oxml.ns import nsdecls, qn

def set_cell_background(cell, fill_hex):
    shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{fill_hex}"/>')
    cell._tc.get_or_add_tcPr().append(shd)

def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
    tcMar = parse_xml(f'''
        <w:tcMar {nsdecls("w")}>
            <w:top w:w="{top}" w:type="dxa"/>
            <w:bottom w:w="{bottom}" w:type="dxa"/>
            <w:left w:w="{left}" w:type="dxa"/>
            <w:right w:w="{right}" w:type="dxa"/>
        </w:tcMar>
    ''')
    cell._tc.get_or_add_tcPr().append(tcMar)

def set_callout_borders(cell, border_color="1B365D", border_size=24):
    tcBorders = parse_xml(f'''
        <w:tcBorders {nsdecls("w")}>
            <w:top w:val="none"/>
            <w:left w:val="single" w:sz="{border_size}" w:space="0" w:color="{border_color}"/>
            <w:bottom w:val="none"/>
            <w:right w:val="none"/>
        </w:tcBorders>
    ''')
    cell._tc.get_or_add_tcPr().append(tcBorders)

def set_code_borders(cell, border_color="D0D7DE"):
    tcBorders = parse_xml(f'''
        <w:tcBorders {nsdecls("w")}>
            <w:top w:val="single" w:sz="4" w:space="0" w:color="{border_color}"/>
            <w:left w:val="single" w:sz="4" w:space="0" w:color="{border_color}"/>
            <w:bottom w:val="single" w:sz="4" w:space="0" w:color="{border_color}"/>
            <w:right w:val="single" w:sz="4" w:space="0" w:color="{border_color}"/>
        </w:tcBorders>
    ''')
    cell._tc.get_or_add_tcPr().append(tcBorders)

def set_table_borders(table, border_color="D0D7DE"):
    tblPr = table._tbl.tblPr
    borders = parse_xml(f'''
        <w:tblBorders {nsdecls("w")}>
            <w:top w:val="single" w:sz="6" w:space="0" w:color="{border_color}"/>
            <w:bottom w:val="single" w:sz="6" w:space="0" w:color="{border_color}"/>
            <w:insideH w:val="single" w:sz="4" w:space="0" w:color="{border_color}"/>
            <w:insideV w:val="none"/>
            <w:left w:val="none"/>
            <w:right w:val="none"/>
        </w:tblBorders>
    ''')
    tblPr.append(borders)

def add_title(doc, text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(2)
    run = p.add_run(text)
    run.font.name = "Segoe UI"
    run.font.size = Pt(22)
    run.font.bold = True
    run.font.color.rgb = RGBColor(27, 54, 93) # Deep Navy
    return p

def add_subtitle(doc, text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(14)
    run = p.add_run(text)
    run.font.name = "Segoe UI"
    run.font.size = Pt(11)
    run.font.color.rgb = RGBColor(90, 100, 115)
    return p

def add_h1(doc, text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(16)
    p.paragraph_format.space_after = Pt(4)
    p.paragraph_format.keep_with_next = True
    run = p.add_run(text)
    run.font.name = "Segoe UI"
    run.font.size = Pt(15)
    run.font.bold = True
    run.font.color.rgb = RGBColor(27, 54, 93)
    return p

def add_h2(doc, text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(12)
    p.paragraph_format.space_after = Pt(3)
    p.paragraph_format.keep_with_next = True
    run = p.add_run(text)
    run.font.name = "Segoe UI"
    run.font.size = Pt(12)
    run.font.bold = True
    run.font.color.rgb = RGBColor(46, 117, 182) # Medium Blue
    return p

def add_h3(doc, text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(8)
    p.paragraph_format.space_after = Pt(2)
    p.paragraph_format.keep_with_next = True
    run = p.add_run(text)
    run.font.name = "Segoe UI"
    run.font.size = Pt(10.5)
    run.font.bold = True
    run.font.color.rgb = RGBColor(50, 50, 50)
    return p

def add_p(doc, text, bold_prefix=None, space_after=4):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(space_after)
    p.paragraph_format.line_spacing = 1.15
    if bold_prefix:
        r_prefix = p.add_run(bold_prefix)
        r_prefix.font.name = "Segoe UI"
        r_prefix.font.size = Pt(10)
        r_prefix.font.bold = True
        r_prefix.font.color.rgb = RGBColor(30, 30, 30)
    
    r_body = p.add_run(text)
    r_body.font.name = "Segoe UI"
    r_body.font.size = Pt(10)
    r_body.font.color.rgb = RGBColor(40, 40, 40)
    return p

def add_bullet(doc, text, bold_prefix=None):
    p = doc.add_paragraph(style='List Bullet')
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(3)
    p.paragraph_format.line_spacing = 1.15
    if bold_prefix:
        r_prefix = p.add_run(bold_prefix)
        r_prefix.font.name = "Segoe UI"
        r_prefix.font.size = Pt(10)
        r_prefix.font.bold = True
        r_prefix.font.color.rgb = RGBColor(30, 30, 30)
    r_body = p.add_run(text)
    r_body.font.name = "Segoe UI"
    r_body.font.size = Pt(10)
    r_body.font.color.rgb = RGBColor(40, 40, 40)
    return p

def add_callout(doc, tag, text, style="concept"):
    table = doc.add_table(rows=1, cols=1)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    table.columns[0].width = Inches(6.8)
    cell = table.cell(0, 0)
    
    if style == "concept":
        bg_color = "F0F4F8"
        border_color = "1B365D"
        tag_color = RGBColor(27, 54, 93)
    elif style == "tip":
        bg_color = "F0FDF4"
        border_color = "16A34A"
        tag_color = RGBColor(22, 163, 74)
    else:
        bg_color = "FFFBEB"
        border_color = "D97706"
        tag_color = RGBColor(217, 119, 6)

    set_cell_background(cell, bg_color)
    set_callout_borders(cell, border_color, border_size=28)
    set_cell_margins(cell, top=120, bottom=120, left=180, right=180)
    
    p = cell.paragraphs[0]
    p.paragraph_format.space_before = Pt(2)
    p.paragraph_format.space_after = Pt(2)
    p.paragraph_format.line_spacing = 1.15
    
    r_tag = p.add_run(f"💡 {tag}: ")
    r_tag.font.name = "Segoe UI"
    r_tag.font.size = Pt(9.5)
    r_tag.font.bold = True
    r_tag.font.color.rgb = tag_color
    
    r_text = p.add_run(text)
    r_text.font.name = "Segoe UI"
    r_text.font.size = Pt(9.5)
    r_text.font.color.rgb = RGBColor(45, 45, 45)
    
    # spacing paragraph after table
    sp = doc.add_paragraph()
    sp.paragraph_format.space_before = Pt(0)
    sp.paragraph_format.space_after = Pt(4)

def add_code(doc, code_str, caption=None):
    if caption:
        cp = doc.add_paragraph()
        cp.paragraph_format.space_before = Pt(6)
        cp.paragraph_format.space_after = Pt(2)
        cp.paragraph_format.keep_with_next = True
        r_cap = cp.add_run(f"Listing: {caption}")
        r_cap.font.name = "Segoe UI"
        r_cap.font.size = Pt(8.5)
        r_cap.font.bold = True
        r_cap.font.color.rgb = RGBColor(80, 80, 80)

    table = doc.add_table(rows=1, cols=1)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    table.columns[0].width = Inches(6.8)
    cell = table.cell(0, 0)
    
    set_cell_background(cell, "F8F9FA")
    set_code_borders(cell, "E1E4E8")
    set_cell_margins(cell, top=100, bottom=100, left=140, right=140)
    
    lines = code_str.strip().split("\n")
    for i, line in enumerate(lines):
        if i == 0:
            p = cell.paragraphs[0]
        else:
            p = cell.add_paragraph()
        p.paragraph_format.space_before = Pt(0)
        p.paragraph_format.space_after = Pt(0)
        p.paragraph_format.line_spacing = Pt(11)
        
        run = p.add_run(line if line else " ")
        run.font.name = "Consolas"
        run.font.size = Pt(8.5)
        run.font.color.rgb = RGBColor(36, 41, 46)
        
    sp = doc.add_paragraph()
    sp.paragraph_format.space_before = Pt(0)
    sp.paragraph_format.space_after = Pt(4)

def add_table(doc, headers, rows):
    table = doc.add_table(rows=len(rows) + 1, cols=len(headers))
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    set_table_borders(table)
    
    # Header row
    hdr_cells = table.rows[0].cells
    for i, title in enumerate(headers):
        hdr_cells[i].text = title
        set_cell_background(hdr_cells[i], "1B365D")
        set_cell_margins(hdr_cells[i], top=100, bottom=100, left=120, right=120)
        p = hdr_cells[i].paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.LEFT
        for run in p.runs:
            run.font.name = "Segoe UI"
            run.font.size = Pt(9.5)
            run.font.bold = True
            run.font.color.rgb = RGBColor(255, 255, 255)
            
    # Data rows
    for r_idx, row in enumerate(rows):
        row_cells = table.rows[r_idx + 1].cells
        bg_col = "F8FAFC" if r_idx % 2 == 1 else "FFFFFF"
        for c_idx, val in enumerate(row):
            row_cells[c_idx].text = str(val)
            set_cell_background(row_cells[c_idx], bg_col)
            set_cell_margins(row_cells[c_idx], top=80, bottom=80, left=120, right=120)
            p = row_cells[c_idx].paragraphs[0]
            for run in p.runs:
                run.font.name = "Segoe UI"
                run.font.size = Pt(9)
                run.font.color.rgb = RGBColor(40, 40, 40)
                
    sp = doc.add_paragraph()
    sp.paragraph_format.space_before = Pt(0)
    sp.paragraph_format.space_after = Pt(6)

def generate_guide():
    doc = Document()
    
    for section in doc.sections:
        section.top_margin = Inches(0.8)
        section.bottom_margin = Inches(0.8)
        section.left_margin = Inches(0.85)
        section.right_margin = Inches(0.85)

    # Title & Subtitle
    add_title(doc, "Java Chess Engine: Architecture & Beginner's Guide")
    add_subtitle(doc, "A Clear, Step-by-Step Explanation of How a Chess AI Works in Plain Java 17")
    
    # Overview
    add_h1(doc, "1. Introduction: How Does a Computer Play Chess?")
    add_p(doc, "When humans play chess, we use visual intuition, memory of past games, and pattern recognition. A computer cannot 'see' a board or feel intuition. Instead, a chess engine relies entirely on deterministic algorithms and structured data.")
    add_p(doc, "At its core, a complete chess engine is made of five distinct systems working together in a loop:")
    
    add_bullet(doc, "Stores the positions of the 32 pieces, castling rights, turn order, and move history.", "1. Board Model (State): ")
    add_bullet(doc, "Calculates all legal moves following official FIDE chess rules, ensuring the King is never left in check.", "2. Move Generator: ")
    add_bullet(doc, "Assigns a numerical score to a static board (e.g. +300 means White has an advantage equal to 3 pawns).", "3. Evaluator: ")
    add_bullet(doc, "Simulates millions of future moves and counter-moves to find the optimal line of play using Minimax and Alpha-Beta pruning.", "4. Search Algorithm: ")
    add_bullet(doc, "Presents an interactive Swing GUI window or terminal CLI for human players to play against the computer.", "5. User Interface: ")

    add_callout(doc, "Beginner Concept: Zero Dependencies", 
                "This entire chess engine is built in 100% pure standard Java (JDK 17). It uses no third-party libraries, no external frameworks, and no complex build tools like Maven or Gradle. Everything is implemented directly using Java's built-in collections, object-oriented principles, and standard Swing GUI libraries.",
                style="tip")

    # Chapter 2: The Virtual Chessboard
    add_h1(doc, "2. The Virtual Chessboard (Package: chess.model)")
    add_p(doc, "The first design decision in building a chess engine is how to represent the 64 squares of a chessboard in computer memory.")

    add_h2(doc, "The 1D Array Coordinate System")
    add_p(doc, "A beginner might naturally try to create an 8x8 two-dimensional array, like Piece[8][8]. While this looks like a physical board, in Java it creates 8 separate array objects in memory, requiring double array lookups (board[row][col]) and 2D nested loops.")
    add_p(doc, "Instead, our engine uses a single, flat one-dimensional array of 64 elements: Piece[64].")
    add_p(doc, "Squares are numbered 0 through 63, starting from a1 (bottom-left) to h8 (top-right):")
    add_bullet(doc, "Square 0 = a1 (White's queenside rook)", "• ")
    add_bullet(doc, "Square 7 = h1 (White's kingside rook)", "• ")
    add_bullet(doc, "Square 56 = a8 (Black's queenside rook)", "• ")
    add_bullet(doc, "Square 63 = h8 (Black's kingside rook)", "• ")

    add_callout(doc, "The Math Behind 1D Coordinates",
                "Converting between a 1D square index (0..63) and 2D rank (row) and file (column) is done using simple integer division and modulo:\n"
                "• rank = square / 8;  (e.g., square 28 / 8 = 3, which is Rank 4 in chess)\n"
                "• file = square % 8;  (e.g., square 28 % 8 = 4, which is File 'e' in chess)\n"
                "• square = rank * 8 + file; (reconstructs the single index)")

    add_code(doc, 
"""// From Square.java: Converting coordinates and names cleanly
public static int fromCoords(int rank, int file) {
    return rank * 8 + file;
}

public static int rankOf(int square) {
    return square / 8;
}

public static int fileOf(int square) {
    return square % 8;
}

public static String toName(int square) {
    char file = (char) ('a' + fileOf(square));
    char rank = (char) ('1' + rankOf(square));
    return \"\" + file + rank; // e.g. 28 -> \"e4\"
}""", caption="Square.java — Rank/File coordinate conversion")

    add_h2(doc, "Enums for Safety & Readability")
    add_p(doc, "Beginners often use raw numbers or text strings to track colors and piece types (e.g. \"white\" or 1). This is dangerous because typos like \"White\" or \"whtie\" cannot be caught by the compiler.")
    add_p(doc, "Our engine uses strongly-typed Java Enums:")
    add_bullet(doc, "WHITE, BLACK. Includes an opposite() helper: (this == WHITE ? BLACK : WHITE).", "PieceColor: ")
    add_bullet(doc, "PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING. Stores base material values (e.g. PAWN = 100, QUEEN = 900).", "PieceType: ")
    add_bullet(doc, "NORMAL, CASTLE, EN_PASSANT, PROMOTION. Informs the board what special mechanics apply.", "MoveFlag: ")

    add_h2(doc, "Polymorphism: The Piece Hierarchy")
    add_p(doc, "In Java, Polymorphism allows us to define a shared base class (Piece) with an abstract method, and let each individual piece implement its own unique movement rules.")
    add_p(doc, "Instead of a giant, messy switch statement checking every piece type, our engine defines:")
    
    add_code(doc,
"""public abstract class Piece {
    private final PieceColor color;
    private final PieceType type;

    // Every piece subclass MUST implement this method:
    public abstract List<Move> generateMoves(Board board, int square);
}""", caption="Piece.java — Polymorphic base class")

    add_p(doc, "The subclasses implement the specific geometry of chess:")
    add_bullet(doc, "Steps 1 square forward, or 2 squares on its first move. Captures diagonally. Can promote or capture en passant.", "Pawn: ")
    add_bullet(doc, "Jumps in an 'L-shape' (2 squares one way, 1 square the other). Because it jumps, it ignores pieces in between.", "Knight: ")
    add_bullet(doc, "Slides diagonally along rays (+9, +7, -7, -9) until blocked.", "Bishop: ")
    add_bullet(doc, "Slides orthogonally along rays (+8, -8, +1, -1) until blocked.", "Rook: ")
    add_bullet(doc, "Combines both Bishop and Rook movements (all 8 directions).", "Queen: ")
    add_bullet(doc, "Moves 1 square in any direction, plus checks castling eligibility.", "King: ")

    add_p(doc, "Let's look at how the Knight calculates its moves using delta offsets:")

    add_code(doc,
"""// From Knight.java: 8 possible 'L-shaped' jump offsets
private static final int[] OFFSETS = {-17, -15, -10, -6, 6, 10, 15, 17};

@Override
public List<Move> generateMoves(Board board, int square) {
    List<Move> moves = new ArrayList<>();
    int fromRank = Square.rankOf(square);
    int fromFile = Square.fileOf(square);

    for (int offset : OFFSETS) {
        int target = square + offset;
        if (target < 0 || target >= 64) continue; // Off the board

        int toRank = Square.rankOf(target);
        int toFile = Square.fileOf(target);

        // Prevent wrapping around the board edges:
        if (Math.abs(fromRank - toRank) <= 2 && Math.abs(fromFile - toFile) <= 2) {
            Piece dest = board.getPiece(target);
            if (dest == null) {
                moves.add(new Move(square, target, this, null, null, MoveFlag.NORMAL));
            } else if (dest.getColor() != getColor()) {
                moves.add(new Move(square, target, this, dest, null, MoveFlag.NORMAL));
            }
        }
    }
    return moves;
}""", caption="Knight.java — 8 jump offsets with board-edge wrap prevention")

    add_h2(doc, "Castling Rights Bitmask")
    add_p(doc, "In chess, both players can potentially castle Kingside or Queenside. That gives 4 independent true/false flags. Instead of creating 4 boolean variables (boolean whiteCastleKing, whiteCastleQueen, etc.), our engine packs them into a single integer bitmask:")
    add_bullet(doc, "Bit 0 (1): White Kingside (O-O)", "• ")
    add_bullet(doc, "Bit 1 (2): White Queenside (O-O-O)", "• ")
    add_bullet(doc, "Bit 2 (4): Black Kingside (O-O)", "• ")
    add_bullet(doc, "Bit 3 (8): Black Queenside (O-O-O)", "• ")
    add_p(doc, "At the start of the game, all 4 rights are available: 1 + 2 + 4 + 8 = 15 (binary 1111). When a Rook or King moves, we remove rights in one CPU cycle using bitwise AND: castlingRights &= ~WHITE_OO;")

    add_h2(doc, "The In-Place makeMove and unmakeMove Pattern")
    add_p(doc, "When the computer looks ahead into the future, it might examine 2 million possible positions in 2 seconds. A beginner might write board.copy() before testing each move. But creating 2,000,000 board objects in Java causes massive memory allocation and triggers the Garbage Collector, slowing the program down to a crawl.")
    add_p(doc, "Instead, chess engines use the makeMove / unmakeMove pattern:")
    add_bullet(doc, "1. Apply the move directly to the existing board array.", "makeMove: ")
    add_bullet(doc, "2. Push the previous board state (captured piece, castling rights, en passant file) onto an undo stack.", "Record State: ")
    add_bullet(doc, "3. Recursively search the new position.", "Search: ")
    add_bullet(doc, "4. Revert the board back to its exact previous state by popping from the undo stack.", "unmakeMove: ")

    add_code(doc,
"""// In Board.java: Reversible board transitions
public void makeMove(Move move) {
    history.push(new BoardState(castlingRights, enPassantSquare, halfmoveClock, capturedPiece));
    // update squares array...
}

public void unmakeMove(Move move) {
    BoardState prev = history.pop();
    this.castlingRights = prev.castlingRights;
    this.enPassantSquare = prev.enPassantSquare;
    this.halfmoveClock = prev.halfmoveClock;
    // restore piece positions...
}""", caption="Board.java — In-place move simulation with zero object churn")

    # Chapter 3: Move Generation & Attack Detection
    add_h1(doc, "3. Move Generation & Attack Detection (Package: chess.movegen)")
    add_p(doc, "Generating legal moves is one of the most bug-prone parts of writing a chess engine. If the engine misses a legal move, it might miss checkmate. If it generates an illegal move, it will crash.")

    add_h2(doc, "Pseudo-Legal vs. Strictly Legal Moves")
    add_p(doc, "To maximize search speed, move generation is split into two clean stages:")
    add_bullet(doc, "Generates moves that follow the piece's geometry without worrying yet about whether our own King is in check.", "Stage 1 (Pseudo-Legal): ")
    add_bullet(doc, "Tests each candidate move: board.makeMove(m); if (!inCheck) legalMoves.add(m); board.unmakeMove(m);", "Stage 2 (Legality Filter): ")

    add_h2(doc, "How AttackDetector Works (Working Backwards)")
    add_p(doc, "How does the engine know if the White King is in check? A naive approach would be to calculate all possible moves for all 16 Black pieces and see if any land on the King. This is very slow.")
    add_p(doc, "Instead, AttackDetector works backwards from the King's square:")
    add_bullet(doc, "Checks if any of the 8 knight jump offsets from the King contain an enemy knight.", "• ")
    add_bullet(doc, "Shoots diagonal rays outward from the King. If it hits an enemy Bishop or Queen, the King is in check!", "• ")
    add_bullet(doc, "Shoots straight rays outward from the King. If it hits an enemy Rook or Queen, the King is in check!", "• ")
    add_bullet(doc, "Checks the two diagonal forward squares to see if an enemy pawn is attacking.", "• ")
    add_p(doc, "By searching outward from the target square, attack detection completes in a few nanoseconds.")

    # Chapter 4: Evaluation
    add_h1(doc, "4. The Engine's Brain: Board Evaluation (Package: chess.search)")
    add_p(doc, "Once the computer stops searching, it needs to look at the static board and give it a numeric score. This score is measured in 'centipawns' (1 pawn = 100 points).")
    
    add_h2(doc, "1. Material Balance")
    add_p(doc, "The most fundamental evaluation is counting pieces on the board:")
    
    table_headers = ["Piece", "Symbol", "Value in Centipawns", "Description"]
    table_rows = [
        ["Pawn", "P", "100", "Base unit of currency"],
        ["Knight", "N", "320", "Minor piece, dangerous in closed positions"],
        ["Bishop", "B", "330", "Minor piece, slightly stronger than knight in open games"],
        ["Rook", "R", "500", "Major piece, controls open files"],
        ["Queen", "Q", "900", "Most powerful piece on the board"],
        ["King", "K", "20,000", "Invaluable; losing the king is checkmate"]
    ]
    add_table(doc, table_headers, table_rows)

    add_h2(doc, "2. Piece-Square Tables (PST) & PeSTO")
    add_p(doc, "If an engine only counted material, it would play terribly. It would gladly put a knight in the corner on a1 where it is useless, because it still counts as 320 points!")
    add_p(doc, "To give the engine chess 'positional understanding', we use Piece-Square Tables (PeSTO tables). For every piece type, a 64-element table gives a bonus or penalty depending on where the piece sits:")
    add_bullet(doc, "Get huge bonuses for occupying central squares (d4, e4, d5, e5) where they control 8 squares, and heavy penalties on the rim (a1, h1) where they control only 2 squares.", "Knights: ")
    add_bullet(doc, "Get rewarded for advancing forward toward the promotion rank, and for defending the center.", "Pawns: ")
    add_bullet(doc, "In the opening, the King wants to hide safely in the corner behind castled pawns (bonus on g1/c1). But in the endgame, the King wants to march into the center to escort passing pawns!", "Kings: ")

    add_callout(doc, "Negamax Evaluation Perspective",
                "Our evaluator uses the Negamax convention: the returned score is always relative to the player whose turn it is to move. If White is up a Queen and it is White's turn, the score is +900. If White is up a Queen but it is Black's turn, the score is -900 (because Black is in a losing situation).",
                style="concept")

    # Chapter 5: Search
    add_h1(doc, "5. Finding the Best Move: Search Algorithms (Package: chess.search)")
    add_p(doc, "A chess game has an astronomical number of possible moves. On average, a player has ~35 legal moves per turn. Looking ahead just 4 turns (8 half-moves) means exploring 35^8 = 2.25 trillion positions! A brute force search is impossible. The engine must search smartly.")

    add_h2(doc, "Negamax Formulation of Minimax")
    add_p(doc, "Minimax is the classic game-theory algorithm: White wants to maximize the score, and Black wants to minimize it. Negamax simplifies this by taking advantage of zero-sum symmetry: max(a, b) = -min(-a, -b). A single recursive function handles both players!")

    add_code(doc,
"""// Core Negamax Recursion
int negamax(Board board, int depth, int alpha, int beta, int ply) {
    if (depth == 0) return quiescence(board, alpha, beta);

    List<Move> moves = board.generateLegalMoves();
    if (moves.isEmpty()) {
        // If in check, checkmate! Prefer shorter mates:
        return board.isInCheck() ? (-CHECKMATE_SCORE + ply) : 0; // 0 = Stalemate
    }

    int bestScore = -INFINITY;
    for (Move move : moves) {
        board.makeMove(move);
        int score = -negamax(board, depth - 1, -beta, -alpha, ply + 1);
        board.unmakeMove(move);

        if (score > bestScore) bestScore = score;
        if (score > alpha) alpha = score;
        if (alpha >= beta) break; // Alpha-Beta Pruning (Cutoff)
    }
    return bestScore;
}""", caption="Search.java — Negamax search with Alpha-Beta pruning & mate distance")

    add_callout(doc, "Why Subtract Ply from Checkmate Score?",
                "Notice: return -CHECKMATE_SCORE + ply;\n"
                "Why add/subtract 'ply' (the number of moves from the start of the search)?\n"
                "If Checkmate is worth 20,000 points, checkmate in 1 move is scored as 20,000 - 1 = 19,999. Checkmate in 5 moves is scored as 20,000 - 5 = 19,995. Because 19,999 > 19,995, the engine will ALWAYS choose the fastest, most direct checkmate possible!",
                style="tip")

    add_h2(doc, "Alpha-Beta Pruning: The 90% Speedup")
    add_p(doc, "Alpha-Beta pruning allows the engine to completely skip exploring branches of the tree that are mathematically guaranteed to have no impact on the final decision.")
    add_bullet(doc, "The minimum score the current player is already guaranteed to achieve from another branch.", "Alpha (α): ")
    add_bullet(doc, "The maximum score the opponent will ever permit us to achieve.", "Beta (β): ")
    add_p(doc, "If a move produces a score greater than or equal to Beta (alpha >= beta), we stop searching immediately (called a Beta Cutoff). Why? Because our opponent already has an earlier move that prevents us from ever reaching this favorable position!")

    add_callout(doc, "Real-Life Analogy for Alpha-Beta Pruning",
                "Imagine you are comparing two vacation hotels. Hotel A is rated 8/10 across all reviews. You begin looking at Hotel B, and the very first review shows bedbugs and mold (rated 1/10). Do you need to spend 2 hours reading the remaining 50 reviews of Hotel B? No! You already know you will never choose Hotel B over Hotel A.",
                style="tip")

    add_h2(doc, "Move Ordering: Most Valuable Victim - Least Valuable Aggressor (MVV-LVA)")
    add_p(doc, "Alpha-Beta pruning works best when the best moves are searched FIRST. If you search the winning move first, all other moves get immediately pruned away.")
    add_p(doc, "To order moves, our engine uses the MVV-LVA heuristic:")
    add_bullet(doc, "Pawn takes Queen (Victim = 900, Aggressor = 100) -> Score = 900 - 100 = 800 (Searched First!)", "1. ")
    add_bullet(doc, "Knight takes Queen (Victim = 900, Aggressor = 320) -> Score = 900 - 320 = 580", "2. ")
    add_bullet(doc, "Pawn takes Pawn (Victim = 100, Aggressor = 100) -> Score = 100 - 100 = 0", "3. ")
    add_bullet(doc, "Queen takes Pawn (Victim = 100, Aggressor = 900) -> Score = 100 - 900 = -800 (Searched Last)", "4. ")

    add_h2(doc, "Quiescence Search: Solving the Horizon Effect")
    add_p(doc, "What happens if a normal search stops at depth 4, right after White captures Black's pawn with their Queen? The static evaluation will say: 'White is up +100 points! Great move!'")
    add_p(doc, "It completely misses that on move 5, Black's bishop can recapture White's Queen, losing 900 points! This is called the Horizon Effect.")
    add_p(doc, "Quiescence Search solves this: when normal search reaches depth 0, it doesn't immediately stop. Instead, it continues searching only captures and tactical exchanges until the board is 'quiet' (no pieces under immediate attack).")

    # Chapter 6: Optimization & Memory
    add_h1(doc, "6. Optimization & Memory (Package: chess.hash)")

    add_h2(doc, "Zobrist Hashing (64-Bit Bitwise Math)")
    add_p(doc, "Every chess position needs a unique digital fingerprint so the computer can recognize when it has seen the exact same board before.")
    add_p(doc, "Zobrist Hashing solves this using bitwise XOR (^):")
    add_bullet(doc, "At startup, the engine generates an array of 64-bit random numbers (long) for every piece on every square: long[12][64].", "1. Setup: ")
    add_bullet(doc, "When a piece moves from square A to square B, we XOR the piece out of A, and XOR it into B:", "2. Lightning Fast Update: ")
    add_p(doc, "hash = hash ^ Zobrist.PIECE_SQUARE[piece][A] ^ Zobrist.PIECE_SQUARE[piece][B];")
    add_p(doc, "Because XOR is self-inverting (x ^ x = 0), updating the board's hash takes less than 1 nanosecond, without looping over the 64 squares!")

    add_h2(doc, "Transposition Table (Memoization)")
    add_p(doc, "In chess, different move orders often lead to the exact same board position. For example:")
    add_bullet(doc, "Order 1: 1. e4 e5 2. Nf3 Nc6", "• ")
    add_bullet(doc, "Order 2: 1. Nf3 Nc6 2. e4 e5", "• ")
    add_p(doc, "Both reach the identical position! The Transposition Table is a massive fixed-size hash table in RAM. If the engine reaches a position it already evaluated at the same depth, it reuses the score immediately, pruning entire subtrees without calculating them again.")

    # Chapter 7: GUI & Concurrency
    add_h1(doc, "7. The User Interface & Multithreading (Package: chess.ui)")
    
    add_h2(doc, "Swing Graphics & Click-to-Move")
    add_p(doc, "The visual chessboard (BoardPanel) extends Swing's JPanel and overrides paintComponent(Graphics g):")
    add_bullet(doc, "Draws an 8x8 alternating grid using wood-toned colors (#DDB88C for light squares, #A66D40 for dark squares).", "Board: ")
    add_bullet(doc, "Renders high-resolution Unicode chess characters (♔, ♕, ♖, ♗, ♘, ♙).", "Pieces: ")
    add_bullet(doc, "First click selects a piece (green highlight) and highlights valid destination squares (yellow dots). Second click executes the move.", "Click-to-Move: ")

    add_h2(doc, "Why SwingWorker is Essential for AI")
    add_p(doc, "A common beginner bug in Java GUI programming is running heavy calculations directly inside an ActionListener or MouseListener. If the AI searches for 3 seconds on the Event Dispatch Thread (EDT), the entire window freezes, buttons stop clicking, and Windows shows '(Not Responding)'.")
    add_p(doc, "Our engine solves this using SwingWorker:")
    add_bullet(doc, "Runs the heavy Negamax search in a separate background worker thread.", "doInBackground(): ")
    add_bullet(doc, "Automatically executed back on the EDT when the AI finishes, cleanly updating the screen with the AI's move.", "done(): ")

    add_code(doc,
"""// In BoardPanel.java: Running the AI asynchronously
private void makeAiMove() {
    new SwingWorker<SearchResult, Void>() {
        @Override
        protected SearchResult doInBackground() {
            // Heavy search runs on a background thread (EDT remains responsive):
            return search.search(board, searchDepth, timeLimitMs);
        }

        @Override
        protected void done() {
            try {
                SearchResult result = get();
                if (result != null && result.getBestMove() != null) {
                    board.makeMove(result.getBestMove());
                    repaint(); // Safe GUI update back on the EDT
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }.execute();
}""", caption="BoardPanel.java — Non-blocking AI move search with SwingWorker")

    # Chapter 8: Java Concepts Summary Table
    add_h1(doc, "8. Summary: Java Concepts Applied in This Engine")
    add_p(doc, "Building this chess engine touches nearly every major concept in computer science and Java programming:")

    sum_headers = ["Chess Engine Feature", "Java Implementation", "Core Concept Learned"]
    sum_rows = [
        ["Board Coordinates", "1D array indices (0..63)", "Integer division (/) and modulo (%)"],
        ["Colors & Pieces", "Enums with methods & fields", "Type safety, avoiding stringly-typed bugs"],
        ["Piece Movement", "Piece abstract class & subclasses", "Polymorphism, Inheritance, Encapsulation"],
        ["Move Representation", "Immutable Move class with final fields", "Immutability, defensive programming"],
        ["Board State History", "Stack of BoardState records", "LIFO Data Structures (push/pop)"],
        ["Game Tree Lookahead", "Recursive Negamax function", "Recursion, Backtracking, Tree Traversal"],
        ["Branch Pruning", "Alpha-Beta condition checks", "Algorithm optimization, Search complexity"],
        ["Position Hashing", "Bitwise XOR (^) on 64-bit longs", "Bit manipulation, Fast Hash Functions"],
        ["Cache Table", "Fixed-size TranspositionTable array", "Hash Tables, Index Masking (&)"],
        ["Responsive GUI", "SwingWorker background task", "Concurrency, Multithreading, Thread Safety"]
    ]
    add_table(doc, sum_headers, sum_rows)

    out_path = os.path.join(r"c:\Users\jeffe\Downloads\javaChessEngine", "JavaChessEngine_Beginners_Guide.docx")
    doc.save(out_path)
    print(f"Successfully generated: {out_path}")

if __name__ == "__main__":
    generate_guide()
