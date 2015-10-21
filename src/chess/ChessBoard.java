/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess;

import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author Pifko
 */
public class ChessBoard {

    boolean whiteOnTurn = true;
    Piece[][] chessboard = new Piece[8][8];

    public ChessBoard() {
        reset();
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
        
        return boardlist.toString();
    
    }
}
