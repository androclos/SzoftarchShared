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
class Knight extends Piece {

    Knight(boolean white) {
        super(white);
    }

    int move(Cell c0, Cell c1, LinkedList list) {
        list.clear();
        if (c0.i == c1.i && c0.j == c1.j) {
            return MoveResult.MR_BAD;
        }

        int di = Math.abs(c0.i - c1.i);
        int dj = Math.abs(c0.j - c1.j);
        if (di + dj == 3 && di * dj != 0) {
            return MoveResult.MR_OK;
        }
        return MoveResult.MR_BAD;
    }

    public String toString() {
        if (white) {
            return "N";
        }
        return "n";
    }
}
