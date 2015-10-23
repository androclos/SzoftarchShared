/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import chess.ChessBoard;
import chess.Piece;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.Game;

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
    
    public User Login(String name, String password) throws SQLException{
        
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
    
   public void saveGame(Integer whiteid, Integer blackid, Integer currentturn, String startdate, ChessBoard board) throws SQLException{ //untested
   
        conn = DriverManager.getConnection(DB_URL, USER, PASS);
        String query = "INSERT INTO unfinishedgames(white_userid, black_userid, currentturn_userid, gamestartdate) VALUES(?, ?, ?, ?)";
        
        prepstat = conn.prepareStatement(query);
        prepstat.setInt(1, whiteid);
        prepstat.setInt(2, blackid);
        prepstat.setInt(3, currentturn);
        prepstat.setString(4, startdate);
        
        prepstat.executeUpdate();
        
        /////////////////////////////
        
        query = "Select unfinishedgamesid FROM unfinishedgames WHERE white_userid = ? AND black_userid = ? AND gamestartdate=?";
        prepstat = conn.prepareStatement(query);
        
        prepstat.setInt(1, whiteid);
        prepstat.setInt(2, blackid);
        prepstat.setString(3, startdate);
        
        rs = prepstat.executeQuery();
        
        Integer gameddtabaseid = 0;
        while(rs.next()){
            gameddtabaseid = rs.getInt("unfinishedgamesid");
        }
        
        ////////////////////////////////
        query =  "INSERT INTO gamepieces(gameid_unfinishedgameid, type, coordi, coordj) VALUES(?, ?, ?, ?)";
        Object[][] bd = board.getChessboard();
        
        prepstat = conn.prepareStatement(query);
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
               
                if(bd[i][j] != null){    
                    prepstat.setInt(1, gameddtabaseid);
                    prepstat.setString(2, bd[i][j].toString());
                    prepstat.setInt(3, i);
                    prepstat.setInt(4, j);
                    prepstat.addBatch();
                }
            }
        }
        prepstat.executeBatch();   
   
   }
   
   public List<String> usersGame(Integer id) throws SQLException{
   
       
/*SELECT o2.name as black, o3.name as white, o4.name as current
FROM unfinishedgames
JOIN unfinishedgames o1 
LEFT JOIN chess_db.user o2 ON o1.black_userid = o2.userid
LEFT JOIN chess_db.user o3 ON o1.white_userid = o3.userid
LEFT JOIN chess_db.user o4 ON o1.currentturn_userid = o4.userid
WHERE o1.black_userid = ? OR o1.white_userid = ?;  */ //game tablara  nevek
       
        List<String> games = new ArrayList<String>();
   
        conn = DriverManager.getConnection(DB_URL, USER, PASS);
        String query =  "SELECT o2.name as black, o3.name as white, o4.name as current, o1.startdate as date, o1.unfinishedgamesid as gameid "+
                        "FROM unfinishedgames "+
                        "JOIN unfinishedgames o1 "+ 
                        "LEFT JOIN chess_db.user o2 ON o1.black_userid = o2.userid "+
                        "LEFT JOIN chess_db.user o3 ON o1.white_userid = o3.userid "+
                        "LEFT JOIN chess_db.user o4 ON o1.currentturn_userid = o4.userid "+
                        "WHERE o1.black_userid = ? OR o1.white_userid = ?;";
        
        prepstat = conn.prepareStatement(query);
        prepstat.setInt(1, id);
        prepstat.setInt(2, id);
        
        rs = prepstat.executeQuery();
        while(rs.next()){
            String s = rs.getInt("gameid")+"-"+rs.getString("white")+"-"+rs.getString("black")+"-"+rs.getString("current")+"-"+rs.getInt("date");
            games.add(s);
        }
        
        return games;

   }
   
   public List<ChessPiece> loadGame(Integer id) throws SQLException{
   
       List<ChessPiece> pieces = new ArrayList<ChessPiece>();
       
       String query = "Select * FROM gamepieces WHERE gameid_unfinishedgamesid = ?";
       conn = DriverManager.getConnection(DB_URL, USER, PASS);
       
       rs = prepstat.executeQuery();
       
        while(rs.next()){
            ChessPiece p = new ChessPiece(rs.getString("type"), rs.getInt("coordinatei"), rs.getInt("coordinatej"));
            pieces.add(p);
        }
    
       return pieces;
   }
    
}
