import os
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import nsdecls, qn

def set_cell_shading(cell, color_hex):
    shading_xml = f'<w:shd {nsdecls("w")} w:fill="{color_hex}"/>'
    cell._tc.get_or_add_tcPr().append(parse_xml(shading_xml))

def set_cell_margins(cell, top=120, bottom=120, left=180, right=180):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = OxmlElement('w:tcMar')
    for m, val in [('top', top), ('bottom', bottom), ('left', left), ('right', right)]:
        node = OxmlElement(f'w:{m}')
        node.set(qn('w:w'), str(val))
        node.set(qn('w:type'), 'dxa')
        tcMar.append(node)
    tcPr.append(tcMar)

def add_code_block(doc, code_text):
    table = doc.add_table(rows=1, cols=1)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    
    cell = table.cell(0, 0)
    cell.width = Inches(6.8)
    set_cell_shading(cell, "F5F6F8")
    set_cell_margins(cell, top=100, bottom=100, left=150, right=150)
    
    borders_xml = f'''<w:tcBorders {nsdecls("w")}>
        <w:top w:val="single" w:sz="4" w:space="0" w:color="D0D5DD"/>
        <w:left w:val="single" w:sz="24" w:space="0" w:color="2B579A"/>
        <w:bottom w:val="single" w:sz="4" w:space="0" w:color="D0D5DD"/>
        <w:right w:val="single" w:sz="4" w:space="0" w:color="D0D5DD"/>
    </w:tcBorders>'''
    cell._tc.get_or_add_tcPr().append(parse_xml(borders_xml))

    p = cell.paragraphs[0]
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(0)
    p.paragraph_format.line_spacing = 1.05
    
    lines = code_text.splitlines()
    for i, line in enumerate(lines):
        run = p.add_run(line + ("\n" if i < len(lines) - 1 else ""))
        run.font.name = "Consolas"
        run.font.size = Pt(8.5)
        run.font.color.rgb = RGBColor(30, 30, 30)
    
    doc.add_paragraph().paragraph_format.space_after = Pt(4)

def build_docx():
    doc = Document()
    
    for section in doc.sections:
        section.top_margin = Inches(0.8)
        section.bottom_margin = Inches(0.8)
        section.left_margin = Inches(0.8)
        section.right_margin = Inches(0.8)

    title_p = doc.add_paragraph()
    title_p.paragraph_format.space_before = Pt(36)
    title_p.paragraph_format.space_after = Pt(12)
    title_p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    title_run = title_p.add_run("Java Chess Engine")
    title_run.font.name = "Segoe UI"
    title_run.font.size = Pt(28)
    title_run.font.bold = True
    title_run.font.color.rgb = RGBColor(43, 87, 154)

    sub_p = doc.add_paragraph()
    sub_p.paragraph_format.space_after = Pt(24)
    sub_p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    sub_run = sub_p.add_run("Core Engine Source Code & Architecture Specification\nTarget JDK 17 • Eclipse Compatible • Zero External Dependencies")
    sub_run.font.name = "Segoe UI"
    sub_run.font.size = Pt(13)
    sub_run.font.color.rgb = RGBColor(100, 100, 100)

    doc.add_page_break()

    packages = [
        ("Project Configuration", [
            (".project", ".project", "Eclipse project configuration defining the Java builder and natures."),
            (".classpath", ".classpath", "Eclipse classpath setting source root, JavaSE-17, and JUnit 5 container.")
        ]),
        ("chess.model (Board, Pieces & Game State)", [
            ("PieceColor.java", "src/chess/model/PieceColor.java", "Piece color enum with opposite colors and pawn rank/direction helpers."),
            ("PieceType.java", "src/chess/model/PieceType.java", "Piece type enum with character symbols and base material values."),
            ("Square.java", "src/chess/model/Square.java", "0-63 square coordinates, rank/file math, algebraic name conversion."),
            ("MoveFlag.java", "src/chess/model/MoveFlag.java", "Enum for NORMAL, CASTLE, EN_PASSANT, and PROMOTION."),
            ("Move.java", "src/chess/model/Move.java", "Move record containing from, to, pieceMoved, capturedPiece, promotionPiece, and UCI format."),
            ("Piece.java", "src/chess/model/Piece.java", "Abstract base class with flyweight caching and polymorphic move generation."),
            ("Pawn.java", "src/chess/model/Pawn.java", "Pawn moves: single/double push, diagonal captures, en passant, promotions."),
            ("Knight.java", "src/chess/model/Knight.java", "Knight move generation using 8 coordinate offsets."),
            ("Bishop.java", "src/chess/model/Bishop.java", "Bishop move generation using diagonal sliding rays."),
            ("Rook.java", "src/chess/model/Rook.java", "Rook move generation using orthogonal sliding rays."),
            ("Queen.java", "src/chess/model/Queen.java", "Queen move generation combining 8 orthogonal and diagonal rays."),
            ("King.java", "src/chess/model/King.java", "King moves with 8 offsets and castling rights/path clearance checks."),
            ("Board.java", "src/chess/model/Board.java", "8x8 array representation, castling flags, en passant, halfmove clock, undo stack, exact make/unmake mirror, and incremental XOR Zobrist."),
            ("GameState.java", "src/chess/model/GameState.java", "Enum covering ACTIVE, CHECKMATE, STALEMATE, 50-move, insufficient material, repetition."),
            ("GameStateDetector.java", "src/chess/model/GameStateDetector.java", "State determination for checkmate, stalemate, 50-move rule, insufficient material, and threefold repetition.")
        ]),
        ("chess.movegen (Move Generation & Ray Casting)", [
            ("AttackDetector.java", "src/chess/movegen/AttackDetector.java", "Square attack detection for pawns, knights, kings, and sliding pieces."),
            ("MoveGenerator.java", "src/chess/movegen/MoveGenerator.java", "Pseudo-legal move generation and check filtering for fully legal moves.")
        ]),
        ("chess.search (Engine Search & Evaluation)", [
            ("Evaluator.java", "src/chess/search/Evaluator.java", "Evaluation interface defining relative and absolute evaluations."),
            ("MaterialPstEvaluator.java", "src/chess/search/MaterialPstEvaluator.java", "Material balance + PeSTO piece-square tables with mirrored black perspective."),
            ("MoveOrderer.java", "src/chess/search/MoveOrderer.java", "MVV-LVA move ordering heuristic sorting captures and promotions first."),
            ("RootMoveScore.java", "src/chess/search/RootMoveScore.java", "Data class storing root moves and scores for top-N ranking."),
            ("SearchResult.java", "src/chess/search/SearchResult.java", "Container for best move, evaluation, depth, nodes, time, and top moves."),
            ("Search.java", "src/chess/search/Search.java", "Negamax alpha-beta search, quiescence, iterative deepening, time control, TT cutoffs, and mate distance adjustment.")
        ]),
        ("chess.hash (Zobrist & Transposition Table)", [
            ("Zobrist.java", "src/chess/hash/Zobrist.java", "64-bit random tables for pieces x squares, side, 4 castling flags, 8 en passant files."),
            ("RepetitionTracker.java", "src/chess/hash/RepetitionTracker.java", "Two-tier repetition tracker for real-game history and search-time stack."),
            ("TranspositionTable.java", "src/chess/hash/TranspositionTable.java", "Hash table storing depths, scores, exact/lower/upper flags, and PV moves keyed by Zobrist hash.")
        ]),
        ("chess.ui (Swing Graphical & Console Interfaces)", [
            ("BoardPanel.java", "src/chess/ui/BoardPanel.java", "Swing 8x8 chessboard with wood tones, Unicode pieces, highlights, click-to-move, and background AI worker."),
            ("ChessFrame.java", "src/chess/ui/ChessFrame.java", "Main GUI window with menus (Game, Mode, Difficulty, FEN) and live status bar."),
            ("ConsoleGame.java", "src/chess/ui/ConsoleGame.java", "Interactive CLI game with ASCII board rendering, move parsing, and command loop.")
        ])
    ]

    base_dir = r"c:\Users\jeffe\Downloads\javaChessEngine"

    for pkg_title, files in packages:
        h1 = doc.add_heading(level=1)
        h1_run = h1.add_run(pkg_title)
        h1_run.font.name = "Segoe UI"
        h1_run.font.size = Pt(18)
        h1_run.font.bold = True
        h1_run.font.color.rgb = RGBColor(43, 87, 154)
        h1.paragraph_format.space_before = Pt(16)
        h1.paragraph_format.space_after = Pt(8)

        for name, rel_path, desc in files:
            full_path = os.path.join(base_dir, rel_path)
            if not os.path.exists(full_path):
                print(f"File not found: {full_path}")
                continue
            
            with open(full_path, "r", encoding="utf-8") as f:
                code_content = f.read()

            h2 = doc.add_heading(level=2)
            h2_run = h2.add_run(f"{name} ({rel_path})")
            h2_run.font.name = "Segoe UI"
            h2_run.font.size = Pt(13)
            h2_run.font.bold = True
            h2_run.font.color.rgb = RGBColor(30, 30, 30)
            h2.paragraph_format.space_before = Pt(12)
            h2.paragraph_format.space_after = Pt(4)

            desc_p = doc.add_paragraph()
            desc_p.paragraph_format.space_after = Pt(6)
            desc_run = desc_p.add_run(desc)
            desc_run.font.name = "Segoe UI"
            desc_run.font.size = Pt(9.5)
            desc_run.font.italic = True
            desc_run.font.color.rgb = RGBColor(90, 90, 90)

            add_code_block(doc, code_content)

        doc.add_page_break()

    out_path = os.path.join(base_dir, "JavaChessEngine_Complete_Codebase.docx")
    doc.save(out_path)
    print(f"Successfully generated: {out_path}")

if __name__ == "__main__":
    build_docx()
