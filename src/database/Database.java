/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pifko
 */
public class Database {

   String DB_URL;
   String USER;
   String PASS;
   
   Connection conn = null;
   PreparedStatement prepstat = null;
   ResultSet rs = null;
    
    public Database() throws IOException {

           Map<String,String> dbproperties = this.GetconenctionsInfo();
           DB_URL = "jdbc:mysql://" + dbproperties.get("databaseaddress")+":"+dbproperties.get("databaseport")+"/"+dbproperties.get("databasename");
           
           PASS = dbproperties.get("dbpassword");
           USER = dbproperties.get("dbuser"); 

    }

    public static HashMap<String,String> GetconenctionsInfo() throws IOException{
        
        Properties prop = new Properties();
	InputStream input = null;

        input = new FileInputStream("DBconfig.properties");
        prop.load(input);


        HashMap<String,String> properties = new HashMap<String, String>();
        
        for(Map.Entry<Object,Object> entry : prop.entrySet()){          
           properties.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));      
        }
    
        return properties;
    
    }
    
    
    public User getUserById(Integer id) throws SQLException{ //syncronized kene lennie (?)
        
        conn = DriverManager.getConnection(DB_URL, USER, PASS);
        String query = "Select * FROM user";
        
        
        closeConnection();
        return null;
    }
    
    public User getUserByName(String name) throws SQLException{
        
        conn = DriverManager.getConnection(DB_URL, USER, PASS);
        String query = "";
        
        
        closeConnection();
        return null;
    }
    
    public User Login(String password, String name) throws SQLException{
        
        conn = DriverManager.getConnection(DB_URL, USER, PASS);
        String query = "Select * FROM user WHERE name = ? AND password = ?";
        
        prepstat = conn.prepareStatement(query);
        prepstat.setString(1, name);
        prepstat.setString(2, password);
        
        rs = prepstat.executeQuery();
        User u = null;
        
        while(rs.next()){
        
            u = new User(rs.getInt("userid"), rs.getString("name"));

        }
        
        closeConnection();
        
        if(u == null)
            return null;
        else 
            return u;

    }
    
    private void closeConnection() throws SQLException{
   
       if(conn != null)
           conn.close();
       if(prepstat != null)
           prepstat.close();
       if(rs != null)
           rs.close();

   }
    
}
