/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webservice;

import database.Database;
import database.GameOutcome;
import database.User;
import java.sql.SQLException;
import java.util.List;
import javax.jws.WebService;
 


/**
 *
 * @author Pifko
 */

@WebService(endpointInterface = "webservice.WebserviceInterface") //mi az interface
public class WebserviceImpl implements WebserviceInterface{

    private Database db;

    public WebserviceImpl(Database db) {
        this.db = db;
    }

    @Override
    public User[] getHelloWorldAsString(String name, String password) throws SQLException{
        
        Integer userid = -1;
        User u = db.Login(password, name);
        User u2 = db.Login("korte", "korte");
        userid = db.Login(password, name).getUserid();
        
        User [] list = new User[2];
        list[0] = u;
         list[1] = u2;

        return list;

    }

    @Override
    public GameOutcome[] getGameOutcomes() throws SQLException {
        
        List<GameOutcome> outcomelist = db.getGameOutcomes();
        
        GameOutcome[] outcomearray = new GameOutcome[outcomelist.size()];
        
        for(int i = 0; i < outcomelist.size(); i++)
            outcomearray[i] = outcomelist.get(i);
        
        return outcomearray;
        
    }
    
    
    
}
