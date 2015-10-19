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
class Pawn extends Piece {

    Pawn(boolean white) {
        super(white);
    }

    int move(Cell c0, Cell c1, LinkedList list) {
        list.clear();

        if (c0.i == c1.i && c0.j == c1.j) {
            return MoveResult.MR_BAD;
        }

        int di = c0.i - c1.i;
        int dj = Math.abs(c0.j - c1.j);

        if (firstMove && di == 2 * dir && dj == 0) {
            list.add(new Cell(c0.i - dir, c0.j));
            return MoveResult.MR_IFNOTATTACKING;
        }
        if (di == dir) {
            if (dj == 1) {
                return MoveResult.MR_IFATTACKING;
            }
            if (dj == 0) {
                return MoveResult.MR_IFNOTATTACKING;
            }
        }
        return MoveResult.MR_BAD;
    }

    public String toString() {
        if (white) {
            return "P";
        }
        return "p";
    }
}
