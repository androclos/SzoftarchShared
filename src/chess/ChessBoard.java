/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess;

import database.ChessPiece;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Pifko
 */
public class ChessBoard {

    boolean whiteOnTurn = true;
    Piece[][] chessboard = new Piece[8][8];
    public static String defaultboardstate = "r00:n01:b02:q03:k04:b05:n06:r07:p10:p11:p12:p13:p14:p15:p16:p17:P60:P61:P62:P63:P64:P65:P66:P67:R70:N71:B72:Q73:K74:B75:N76:R77:";

    public ChessBoard() {
        reset();
    }
       
    public ChessBoard(List<ChessPiece> list) {
        
        buildBoard(list);
        
    }

    public void setWhiteOnTurn(boolean whiteOnTurn) {
        this.whiteOnTurn = whiteOnTurn;
    }
    
    
    
    public Piece removePiece(Cell c)
    {
     Piece p = getPiece(c);
     chessboard[c.i][c.j] = null;
     return p;
    }
    
	void putPiece(Cell c, Piece p)
    {
    chessboard[c.i][c.j] = p;
    }

    public Piece[][] getChessboard() {
        return chessboard;
    }

	
    public void reset() {
        whiteOnTurn = true;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                chessboard[i][j] = null;
            }
        }

        chessboard[0][0] = new Rook(false);
        chessboard[0][7] = new Rook(false);
        chessboard[7][0] = new Rook(true);
        chessboard[7][7] = new Rook(true);


        chessboard[0][1] = new Knight(false);
        chessboard[0][6] = new Knight(false);
        chessboard[7][1] = new Knight(true);
        chessboard[7][6] = new Knight(true);

        chessboard[0][2] = new Bishop(false);
        chessboard[0][5] = new Bishop(false);
        chessboard[7][2] = new Bishop(true);
        chessboard[7][5] = new Bishop(true);

        chessboard[0][3] = new Queen(false);
        chessboard[0][4] = new King(false);
        chessboard[7][4] = new King(true);
        chessboard[7][3] = new Queen(true);

        for (int i = 0; i < 8; i++) {
            chessboard[1][i] = new Pawn(false);
            chessboard[6][i] = new Pawn(true);
        }
    }

    public Piece getPiece(Cell c) {
        return chessboard[c.i][c.j];
    }

    public int move(Cell c0, Cell c1, boolean doMove) {
        Piece srcPiece = getPiece(c0);
        if (srcPiece == null || srcPiece.white != whiteOnTurn) {
            return 0;
        }

        LinkedList list = new LinkedList();
        //check if piece can move to new cell
        int res = srcPiece.move(c0, c1, list);
        Piece destPiece = getPiece(c1);

        if (res == MoveResult.MR_IFATTACKING) {
            if (destPiece != null && whiteOnTurn != destPiece.white) {
                res = MoveResult.MR_OK;
            }
        }
        if (res == MoveResult.MR_IFNOTATTACKING && destPiece == null) {
            res = MoveResult.MR_OK;
        }
        if (destPiece != null && whiteOnTurn == destPiece.white) {
            res = MoveResult.MR_BAD;
        }

        if (res != MoveResult.MR_OK) {
            return 0;
        }

        Iterator it = list.listIterator();
        while (it.hasNext()) {

            Cell c = (Cell) it.next();
            if (getPiece(c) != null) {
                return 0;
            }

        }

        if (doMove) {
            chessboard[c1.i][c1.j] = chessboard[c0.i][c0.j];
            chessboard[c0.i][c0.j] = null;
        }

        whiteOnTurn = !whiteOnTurn;
        srcPiece.firstMove = false;
        return 1;
    }
    
    @Override
    public String toString(){
    
        StringBuilder boardlist = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if(chessboard[i][j] != null){
                
                    boardlist.append((chessboard[i][j].toString() + String.valueOf(i) + String.valueOf(j)+":"));

                
                }
                
            }
        }
        
        String s = boardlist.toString(); //utolso : levetele
        s = s.substring(0, s.length()-1);
        return s;
    
    }
    
    public void buildBoard(List<ChessPiece> piecelist){
        
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                chessboard[i][j] = null;
            }
        }
    
        for(ChessPiece p : piecelist){
        
            switch(p.getType()){
            
                case "P":{chessboard[p.getCoordi()][p.getCoordj()] = new Pawn(true); break;}
                case "K":{chessboard[p.getCoordi()][p.getCoordj()] = new King(true); break;}
                case "N":{chessboard[p.getCoordi()][p.getCoordj()] = new Knight(true); break;}
                case "Q":{chessboard[p.getCoordi()][p.getCoordj()] = new Queen(true); break;}
                case "R":{chessboard[p.getCoordi()][p.getCoordj()] = new Rook(true); break;}
                case "B":{chessboard[p.getCoordi()][p.getCoordj()] = new Bishop(true); break;}
                
                case "p":{chessboard[p.getCoordi()][p.getCoordj()] = new Pawn(false); break;}
                case "k":{chessboard[p.getCoordi()][p.getCoordj()] = new King(false); break;}
                case "n":{chessboard[p.getCoordi()][p.getCoordj()] = new Knight(false); break;}
                case "q":{chessboard[p.getCoordi()][p.getCoordj()] = new Queen(false); break;}
                case "r":{chessboard[p.getCoordi()][p.getCoordj()] = new Rook(false); break;}
                case "b":{chessboard[p.getCoordi()][p.getCoordj()] = new Bishop(false); break;}
            
            }

        }
    
    }
}
