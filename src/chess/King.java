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
class King extends Piece {

    King(boolean white) {
        super(white);
    }

    int move(Cell c0, Cell c1, LinkedList list) {
        list.clear();
        if (c0.i == c1.i && c0.j == c1.j) {
            return MoveResult.MR_BAD;
        }

        if (Math.abs(c0.i - c1.i) <= 1 && Math.abs(c0.j - c1.j) <= 1) {
            return MoveResult.MR_OK;
        }
        return MoveResult.MR_BAD;
    }

    public String toString() {
        if (white) {
            return "K";
        }
        return "k";
    }
}
