package chess;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

class ChessImageView extends Panel
{  
   private char letters[] = {'a','b','c','d','e','f','g','h'};
    String message = null;
    ChessBoard board;
    HashMap pieceImages = new HashMap();
    Cell selectedCell = null;
    boolean ready = true;
    Cell animPieceCoord;

}

class ChessImageController
{

    ChessBoard board;
    Cell c0 = null;
    Cell c1 = null;
                
    public ChessImageController()
    {        
       board = new ChessBoard();

    

    }

       public void move(){
       
           int result = board.move(c0, c1, false);
       
       }
}
