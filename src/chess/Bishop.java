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
class Bishop extends Piece {

    Bishop(boolean white) {
        super(white);
    }

    int move(Cell c0, Cell c1, LinkedList list) {
        list.clear();

        if (c0.i == c1.i && c0.j == c1.j) {
            return MoveResult.MR_BAD;
        }

        int di = Math.abs(c0.i - c1.i);
        int dj = Math.abs(c0.j - c1.j);
        if (di == dj) {
            int steps = Math.max(di, dj);
            di = (int) MoveResult.signum(c1.i - c0.i);
            dj = (int) MoveResult.signum(c1.j - c0.j);
            for (int i = 1; i < steps; i++) {
                list.add(new Cell(c0.i + di * i, c0.j + dj * i));
            }
            return MoveResult.MR_OK;
        }
        return MoveResult.MR_BAD;
    }

    public String toString() {
        if (white) {
            return "B";
        }
        return "b";
    }
}
