/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

/**
 *
 * @author Pifko
 */
public class ChessPiece {
    
    private String type;
    private Integer coordi;
    private Integer coordj;

    public ChessPiece(String type, Integer coordi) {
        this.type = type;
        this.coordi = coordi;
    }

    public String getType() {
        return type;
    }

    public Integer getCoordi() {
        return coordi;
    }

    public Integer getCoordj() {
        return coordj;
    }
    
    
    
}
