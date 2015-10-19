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
class MoveResult {

    final static int MR_BAD = 0;
    final static int MR_OK = 1;
    final static int MR_IFATTACKING = 2;
    final static int MR_IFNOTATTACKING = 3;

    public static int signum(int a) {
        if (a > 0) {
            return 1;
        } else if (a < 0) {
            return -1;
        } else {
            return 0;
        }
    }
}
