/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess;

/**
 *
 * @author Pifko
 */
class Cell {

    int i;
    int j;

    Cell(int i, int j) {
        this.i = i;
        this.j = j;
    }

    Cell() {
        i = j = 0;
    }

    public String toString() {
        return "i: " + i + " j: " + j;
    }
}