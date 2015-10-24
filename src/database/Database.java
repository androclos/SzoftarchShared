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
   
   public void addNewGame(Integer whiteid, Integer blackid, Integer currentturn, String startdate, ChessBoard board) throws SQLException{
   
        conn = DriverManager.getConnection(DB_URL, USER, PASS);
        String query = "INSERT INTO unfinishedgames(white_userid, black_userid, currentturn_userid, startdate) VALUES(?, ?, ?, ?)";
        
        prepstat = conn.prepareStatement(query);
        prepstat.setInt(1, whiteid);
        prepstat.setInt(2, blackid);
        prepstat.setInt(3, currentturn);
        prepstat.setString(4, startdate);
        
        prepstat.executeUpdate();
        closeConnection();
   }
   
   public Integer getExactGameId(Integer whiteid, Integer blackid, String startdate) throws SQLException{

        String query = "SELECT unfinishedgamesid FROM unfinishedgames WHERE white_userid = ? AND black_userid = ? AND startdate=?";
        prepstat = conn.prepareStatement(query);
        
        prepstat.setInt(1, whiteid);
        prepstat.setInt(2, blackid);
        prepstat.setString(3, startdate);
        
        rs = prepstat.executeQuery();
        
        Integer gamedatabaseid = -1;
        while(rs.next()){
            gamedatabaseid = rs.getInt("unfinishedgamesid");
        }
        
        return gamedatabaseid;
   }
   
   public void updateGameState(Integer gameid, Integer currentplayer) throws SQLException{
   
        String query = " UPDATE unfinishedgames SET currentturn_userid = ? WHERE unfinishedgamesid = ?";
        prepstat = conn.prepareStatement(query);
        
        prepstat.setInt(1, currentplayer);
        prepstat.setInt(2, gameid);
        prepstat.executeUpdate();
        closeConnection();
   }
   
   public void clearGamePieceList(Integer gameid) throws SQLException{
   
        String query = "DELETE FROM gamepieces WHERE gameid_unfinishedgamesid = ?";
        prepstat = conn.prepareStatement(query);
        
        prepstat.setInt(1, gameid);
        prepstat.executeUpdate();
        closeConnection();
   }
   
   public void saveBoard(Integer gameid, ChessBoard board) throws SQLException{
   
        String query =  "INSERT INTO gamepieces(gameid_unfinishedgamesid, type, coordinatei, coordinatej) VALUES(?, ?, ?, ?)";
        Object[][] bd = board.getChessboard();
        
        prepstat = conn.prepareStatement(query);
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
               
                if(bd[i][j] != null){    
                    prepstat.setInt(1, gameid);
                    prepstat.setString(2, bd[i][j].toString());
                    prepstat.setInt(3, i);
                    prepstat.setInt(4, j);
                    prepstat.addBatch();
                }
            }
        }
        prepstat.executeBatch(); 
        closeConnection();
   }
    
   public void saveGame(Integer whiteid, Integer blackid, Integer currentturn, String startdate, ChessBoard board) throws SQLException{ //untested

       addNewGame(whiteid, blackid, currentturn, startdate, board);
 
       Integer gamedatabaseid = getExactGameId(whiteid, blackid, startdate);
 
       this.saveBoard(gamedatabaseid, board);
   }
   
   public List<String> getUsersGameList(Integer userid) throws SQLException{
   
       
/*SELECT o2.name as black, o3.name as white, o4.name as current
FROM unfinishedgames
JOIN unfinishedgames o1 
LEFT JOIN chess_db.user o2 ON o1.black_userid = o2.userid
LEFT JOIN chess_db.user o3 ON o1.white_userid = o3.userid
LEFT JOIN chess_db.user o4 ON o1.currentturn_userid = o4.userid
WHERE o1.black_userid = ? OR o1.white_userid = ?;  */ //game tablara  nevek
       
        List<String> games = new ArrayList<String>();
   
        conn = DriverManager.getConnection(DB_URL, USER, PASS);
        String query =  "SELECT DISTINCT o2.userid as blackid, o2.name as black, o3.userid as whiteid, o3.name as white, o4.userid as currentid, o4.name as current, o1.startdate as date, o1.unfinishedgamesid as gameid "+
                        "FROM unfinishedgames "+
                        "JOIN unfinishedgames o1 "+ 
                        "LEFT JOIN chess_db.user o2 ON o1.black_userid = o2.userid "+
                        "LEFT JOIN chess_db.user o3 ON o1.white_userid = o3.userid "+
                        "LEFT JOIN chess_db.user o4 ON o1.currentturn_userid = o4.userid "+
                        "WHERE o1.black_userid = ? OR o1.white_userid = ?;";
        
        prepstat = conn.prepareStatement(query);
        prepstat.setInt(1, userid);
        prepstat.setInt(2, userid);
        
        rs = prepstat.executeQuery();
        while(rs.next()){
            String s = rs.getInt("gameid")+"-"+rs.getString("white")+"-"+rs.getString("black")+"-"+rs.getString("current")+"-"+rs.getString("date");
            games.add(s);
        }
        closeConnection();
        return games;
   }
   
    public Map<String,String> getGameInformation(Integer gameid) throws SQLException{
   
        Map<String,String> resultmap = new HashMap<String, String>();
        
        conn = DriverManager.getConnection(DB_URL, USER, PASS);
        String query =  "SELECT DISTINCT o2.userid as blackid, o2.name as black, o3.userid as whiteid, o3.name as white, o4.userid as currentid, o4.name as current, o1.startdate as date, o1.unfinishedgamesid as gameid "+
                        "FROM unfinishedgames "+
                        "JOIN unfinishedgames o1 "+ 
                        "LEFT JOIN chess_db.user o2 ON o1.black_userid = o2.userid "+
                        "LEFT JOIN chess_db.user o3 ON o1.white_userid = o3.userid "+
                        "LEFT JOIN chess_db.user o4 ON o1.currentturn_userid = o4.userid "+
                        "WHERE o1.unfinishedgamesid = ?;";
        
        prepstat = conn.prepareStatement(query);
        prepstat.setInt(1, gameid);
        rs = prepstat.executeQuery();

        
        while(rs.next()){
            
            
            
            resultmap.put("blackid",String.valueOf(rs.getInt("blackid")));
            resultmap.put("black",rs.getString("black"));
            resultmap.put("whiteid",String.valueOf(rs.getInt("whiteid")));
            resultmap.put("white",rs.getString("white"));
            resultmap.put("currentid",String.valueOf(rs.getInt("currentid")));
            resultmap.put("current",rs.getString("current"));
            resultmap.put("date",rs.getString("date"));
            resultmap.put("gameid",String.valueOf(rs.getInt("gameid")));
            
            System.out.println(resultmap.get("black"));
            
        }
        
        
        closeConnection();
        return resultmap;
        
   
   }
   
   public List<ChessPiece> loadGame(Integer gameid) throws SQLException{
   
       List<ChessPiece> pieces = new ArrayList<ChessPiece>();
       
       String query = "Select * FROM gamepieces WHERE gameid_unfinishedgamesid = ?";
       conn = DriverManager.getConnection(DB_URL, USER, PASS);
       
       prepstat = conn.prepareStatement(query);
       prepstat.setInt(1, gameid);
       rs = prepstat.executeQuery();
       
        while(rs.next()){
            ChessPiece p = new ChessPiece(rs.getString("type"), rs.getInt("coordinatei"), rs.getInt("coordinatej"));
            pieces.add(p);
        }
       closeConnection();
       return pieces;
   }
    
   public Integer getMaxGameId() throws SQLException{
   
       String query = "Select MAX(unfinishedgamesid) FROM unfinishedgames";
       conn = DriverManager.getConnection(DB_URL, USER, PASS);
       
       prepstat = conn.prepareStatement(query);
       rs = prepstat.executeQuery();
       
       Integer maxid = -1;
       while(rs.next()){
           maxid = rs.getInt("MAX(unfinishedgamesid)");
       }
       closeConnection();
       return maxid;
     
   }
   
   public boolean isNewGame(Integer gameid) throws SQLException{
   
       String query = "Select unfinishedgamesid FROM unfinishedgames WHERE unfinishedgamesid = ?";
       conn = DriverManager.getConnection(DB_URL, USER, PASS);
       
       prepstat = conn.prepareStatement(query);
       prepstat.setInt(1, gameid);
       rs = prepstat.executeQuery();
   
       while(rs.next()){
           return false;
       }
       closeConnection();
       return true;
   }
   
}
