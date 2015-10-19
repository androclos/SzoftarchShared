/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess;

import java.util.LinkedList;

/**
 *
 * @author Pifko
 */
abstract class Piece {

    boolean white;
    int dir;
    boolean firstMove = true;

    Piece(boolean white) {
        this.white = white;
        if (white) {
            dir = 1;
        } else {
            dir = -1;
        }
    }

    abstract int move(Cell c0, Cell c1, LinkedList list);
}
