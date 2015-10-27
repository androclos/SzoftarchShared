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
public class GameOutcome {
    
    Integer gameid;
    String whiteplayername;
    String blackplayername;
    String winnername;
    String enddate;

    public GameOutcome(Integer gameid, String whiteplayername, String blackplayername, String winnername, String enddate) {
        this.gameid = gameid;
        this.whiteplayername = whiteplayername;
        this.blackplayername = blackplayername;
        this.winnername = winnername;
        this.enddate = enddate;
    }

    public Integer getGameid() {
        return gameid;
    }

    public String getWhiteplayername() {
        return whiteplayername;
    }

    public String getBlackplayername() {
        return blackplayername;
    }

    public String getWinnername() {
        return winnername;
    }

    public String getEnddate() {
        return enddate;
    }
    
    
}
