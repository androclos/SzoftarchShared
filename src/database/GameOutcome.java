/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Pifko
 */
@XmlRootElement
public class GameOutcome implements Serializable{
    
    static final long serialVersionUID = 42L;
    
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

    public GameOutcome() {
    }

    public void setGameid(Integer gameid) {
        this.gameid = gameid;
    }

    public void setWhiteplayername(String whiteplayername) {
        this.whiteplayername = whiteplayername;
    }

    public void setBlackplayername(String blackplayername) {
        this.blackplayername = blackplayername;
    }

    public void setWinnername(String winnername) {
        this.winnername = winnername;
    }

    public void setEnddate(String enddate) {
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
