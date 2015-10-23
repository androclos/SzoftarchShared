package server;


import chess.Cell;
import chess.ChessBoard;
import client.Message;
import database.ChessPiece;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Pifko
 */
public class Game implements Runnable{
    
    private List<UserClient> players = new ArrayList<UserClient>(); //jatekvban levo jatekosok
    private List<String> fixedplayernames = new ArrayList<String>(); // egyszer mar csatalkozott jatekosok
    private Map<Integer,String> fixedplayers = new HashMap<Integer,String>(); // egyszer mar csatalkozott jatekosok
    private Map<String,String> playercolor;
    //private String currentturn;
    private Integer currentturnplayerid;
    private Integer gameid; 
    private boolean gamestrated = false;
    private boolean gamehalted = false;
    private boolean loadedgame = false;
    private ArrayBlockingQueue<Message> gamemessageque;
    private Lobby lobby;
    private ChessBoard board;
    private String gamestarttime;

    public Game(ArrayBlockingQueue<Message> gamemessageq, Integer id, Lobby lob, boolean loadgame) {
        
        this.gamemessageque = gamemessageq;
        this.gameid = id;
        this.lobby = lob;
        this.loadedgame = loadgame;
        

    }

    public Integer getGameid() {
        return gameid;
    }
    
    public void broadcast(Message m){
    
        for(UserClient u : players){
        
            try {
                sendmessage(u.getUserid(), m);
            } catch (IOException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        }
    
    }
    
    
    public synchronized void addplayer(UserClient newuser){

        try{
        
            
            
            if(players.size() <1){
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                gamestarttime = dateFormat.format(new Date());
                
                
                players.add(newuser);
                if(!fixedplayernames.contains(newuser.getUsername()));
                    fixedplayernames.add(newuser.getUsername());

                if(fixedplayers.containsKey(newuser.getUserid()) == false)
                    fixedplayers.put(newuser.getUserid(),newuser.getUsername());
                    
                Message msg = new Message("Waiting for player 2.");
                sendmessage(newuser.getUserid(), msg);
                
                System.out.println("Player: "+ this.players.get(0).getUsername() + " joind to game: " + this.gameid + ".");
                System.out.println("New game with id: "+this.gameid+", started by: "+this.players.get(0).getUsername()+ ".");
                
            }
            
            else{
                
                players.add(newuser);
                fixedplayers.put(newuser.getUserid(),newuser.getUsername());
                if(!fixedplayernames.contains(newuser.getUsername()));
                    fixedplayernames.add(newuser.getUsername());
                
                if(fixedplayers.containsKey(newuser.getUserid()) == false)
                    fixedplayers.put(newuser.getUserid(),newuser.getUsername());
                
                this.gamestrated = true;
                
                newgameinit();
                System.out.println(board.toString());
                
                System.out.println("Player: "+ this.players.get(1).getUsername() + " joind to game: " + this.gameid + ".");
                System.out.println("Game: "+this.gameid+", started with: "+this.players.get(0).getUsername() + " and " + this.players.get(1).getUsername()+ ".");
            }
            
        }catch (Exception ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public synchronized Integer numberofplayers(){
    
       return this.players.size();
    
    }
    
    public synchronized void addmessage(Message s){
    
        this.gamemessageque.add(s);
        
    }

    public boolean isGamestrated() {
        return gamestrated;
    }

    
    @Override
    public void run() {
        
        while(true){
        try {
            
            Message newmsg = this.gamemessageque.take();
            System.out.println("Game message: "+newmsg.getMessage());
            
            List<String> message = msgprocess(newmsg);
            msghandler(message);
            
        } catch (InterruptedException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
        
    }
    
    public void playerleaving(Integer id){
    
        try {
  
            this.gamehalted = true;
            UserClient leavingplayer = this.userbyid(id);
            
            Message m1 = new Message("message:You left the game.");
            //sendmessage(id, m1); //exception dob ha nincs kliens kapcsolat hirtelen megszakad (nem csak logout) 

            this.players.remove(this.userbyid(id));
            
            Message m2 = new Message("game:stopped");
            Message m3 = new Message("message:"+leavingplayer.getUsername() + " has left the game, game is halted.");
            for(UserClient u : this.players){

                sendmessage(u.getUserid(), m2);
                sendmessage(u.getUserid(), m3);
                
            }
            
            
        } catch (IOException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public UserClient userbyid(Integer id){
    
        UserClient player = null;
        for(UserClient u : this.players){
        
            if(u.getUserid().equals(id))
                player = u;
            
        }
        
        return player;
    
    }
    
    public UserClient userbyname(String name){
    
        UserClient player = null;
        for(UserClient u : this.players){
        
            if(u.getUsername().equals(name))
                player = u;
            
        }
        
        return player;
    
    }
    
    public boolean ableToJoin(Integer id/*String username*/){
    
        //return (this.fixedplayernames.contains(username) || (this.players.size()==1 && this.fixedplayernames.size() == 1));
    
        return fixedplayers.containsKey(id) || (this.players.size()== 1 && fixedplayers.size() == 1);
    }
    
    public String getotherplayer(Integer id/*String user*/){
    
        /*for(String  s: fixedplayernames)
            if(!s.equals(user))
                return s;
    
        return "";*/
        
        
        String username = "";
        for (Map.Entry<Integer, String> entry : this.fixedplayers.entrySet()){
            
            if(!entry.getKey().equals(id))
                username = entry.getValue();
        
        }
        return username;
        
        
    }
    
    public List<String> msgprocess(Message m){
    
        StringTokenizer st = new StringTokenizer(m.getMessage(),":"); //a:b:b:c uzenete felosztasa
        List<String> msgparts = new ArrayList<String>();      
        int partnum = st.countTokens();
                
        for(int i = 0;i<partnum;i++){
            msgparts.add(st.nextToken());
        }
    
        return msgparts;
    
    }
    
    public void msghandler(List<String> message){
    
        Integer clientid = Integer.valueOf(message.get(0));
        String messageoperation = message.get(1);
        
        switch(messageoperation){
        
            case "move":{
                Cell src = new Cell(Integer.valueOf(message.get(2)),Integer.valueOf(message.get(3)));
                Cell dest = new Cell(Integer.valueOf(message.get(4)),Integer.valueOf(message.get(5)));
                move(clientid,src,dest);
                break;
            }
            case "leavegame":{
                playerleaving(gameid);
                break;
            }
            case "logout":{
                playerleaving(gameid);
                break;
            }
            case "loadgame":{
                loadgame(Integer.valueOf(message.get(2)));
                break;
            }
            
            default:{
                break;
            }
        
        }
    
    }
    
    public void loadgame(){

    }
    
    public void newgameinit() throws IOException{
    
        playercolor = new HashMap<String,String>();
        board = new ChessBoard();
        
        Random rand = new Random();
        if(rand.nextInt(2) == 1){
        
            playercolor.put("white", players.get(0).getUsername());
            playercolor.put("black", players.get(1).getUsername());
            currentturnplayerid = players.get(0).getUserid();
        
        }
        else{
        
            playercolor.put("white", players.get(1).getUsername());
            playercolor.put("black", players.get(0).getUsername());
            currentturnplayerid = players.get(1).getUserid();
            
        }
        
        Message gamestarted = new Message("game:gamestarted");
        Message whitecolor = new Message("color:white");
        Message blackcolor = new Message("color:black");
        
        broadcast(gamestarted);
        sendmessage(userbyname(playercolor.get("black")).getUserid(), blackcolor);
        sendmessage(userbyname(playercolor.get("white")).getUserid(), whitecolor);
        
    }
    
    public void move(Integer id,Cell src, Cell dest){
    
        try{
            
            //if(userbyid(id).getUsername() != currentturn){
            if(id != currentturnplayerid){
                Message notyourturn = new Message("message:Not your turn.");
                sendmessage(id, notyourturn);
                return;

            }
            
            Integer outcome = board.move(src, dest, true);
            
            if(outcome == 1){
            
                //currentturn = getotherplayer(userbyid(id).getUsername());
                for(Integer i : fixedplayers.keySet()){
                
                    if(!i.equals(id))
                        currentturnplayerid = i;
                
                }
                Message succesfulmove = new Message("move:"+src.toString()+":"+dest.toString());
                broadcast(succesfulmove);
            
            }
            else{
        
                Message wrongmove = new Message("message:Invalid move.");
                sendmessage(id, wrongmove);

            }
            
            System.out.println(board.toString());

            
        }catch (IOException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
    
    public void sendmessage(Integer id, Message m) throws IOException{
    
        userbyid(id).getOutputStream().writeObject(m);
    
    }

    public boolean isLoadedgame() {
        return loadedgame;
    }

    public void setLoadedgame(boolean loadedgame) {
        this.loadedgame = loadedgame;
    }
    
    public void loadgame(Integer id){ //nincs kesz
    
        try {
            List<ChessPiece> pieces = lobby.getDatabaseAccess().loadGame(id);
            board.buildBoard(pieces);
            
            
        } catch (SQLException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
    
    
    public void savegametodatabse(){
    
        
    
    }

}
