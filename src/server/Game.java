package server;


import chess.Cell;
import chess.ChessBoard;
import client.Message;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
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
    private Map<String,String> playercolor;
    private String currentturn;
    private Integer gameid; 
    private boolean gamestrated = false;
    private boolean gamehalted = false;
    private boolean loadedgame = false;
    private ArrayBlockingQueue<Message> gamemessageque;
    private Lobby lobby;
    private ChessBoard board;

    public Game(ArrayBlockingQueue<Message> gamemessageq, Integer id, Lobby lob) {
        
        this.gamemessageque = gamemessageq;
        this.gameid = id;
        this.lobby = lob;
        
    }

    public Integer getGameid() {
        return gameid;
    }
    
    public void broadcast(Message m){
    
        for(UserClient u : players){
        
            try {
                u.getOutputStream().writeObject(m);
            } catch (IOException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        }
    
    }
    
    
    public synchronized void addplayer(UserClient newuser){
    
        ObjectOutputStream o;
        
        try{
        
            if(players.size() <1){
                
                players.add(newuser);
                if(!fixedplayernames.contains(newuser.getUsername()));
                    fixedplayernames.add(newuser.getUsername());
                o = players.get(0).getOutputStream();
                Message msg = new Message("Waiting for player 2.");
                o.writeObject(msg);
                
                System.out.println("Player: "+ this.players.get(0).getUsername() + " joind to game: " + this.gameid + ".");
                System.out.println("New game with id: "+this.gameid+", started by: "+this.players.get(0).getUsername()+ ".");
                
            }
            
            else{
                
                players.add(newuser);
                if(!fixedplayernames.contains(newuser.getUsername()));
                    fixedplayernames.add(newuser.getUsername());
                this.gamestrated = true;
                
                o = players.get(0).getOutputStream();
                Message msg1 = new Message("The game has started.");
                o.writeObject(msg1);
                
                o = players.get(1).getOutputStream();
                Message msg2 = new Message("The game has started.");
                o.writeObject(msg2);
                
                
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
            
        } catch (InterruptedException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
        
    }
    
    public void playerleaving(Integer id){
    
        try {
  
            this.gamehalted = true;
            UserClient leavingplayer = this.userbyid(id);
            
            Message m1 = new Message("You left the game.");
            leavingplayer.getOutputStream().writeObject(m1);

            this.players.remove(this.userbyid(id));
            
            Message m2 = new Message(leavingplayer.getUsername() + " has left the game, game halted.");
            for(UserClient u : this.players){

                u.getOutputStream().writeObject(m2);
                
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
    
    public boolean ableToJoin(String username){
    
        return (this.fixedplayernames.contains(username) || (this.players.size()==1 && this.fixedplayernames.size() == 1));
    
    }
    
    public String getotherplayer(String user){
    
        for(String  s: fixedplayernames)
            if(!s.equals(user))
                return s;
    
        return "";
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
            
            default:{
                
                break;
            
            }
        
        }
    
    }
    
    public void newgameinit(){
    
        playercolor = new HashMap<String,String>();
        
        Random rand = new Random();
        if(rand.nextInt(2) == 1){
        
            playercolor.put("white", players.get(0).getUsername());
            playercolor.put("black", players.get(1).getUsername());
            currentturn = players.get(0).getUsername();
        
        }
        else{
        
            playercolor.put("white", players.get(1).getUsername());
            playercolor.put("black", players.get(0).getUsername());
            currentturn = players.get(1).getUsername();
            
        }
    }
    
    public void move(Integer id,Cell src, Cell dest){
    
        try{
            
            if(userbyid(id).getUsername() != currentturn){

                Message notyourturn = new Message("Not your turn.");
                userbyid(id).getOutputStream().writeObject(notyourturn);

            }
            
            Integer outcome = board.move(src, dest, true);
            
            if(outcome == 1){
            
                Message succesfulmove = new Message("");
                broadcast(succesfulmove);
            
            }
            else{
        
                Message wrongmove = new Message("");
                userbyid(id).getOutputStream().writeObject(wrongmove);

            }

            
        }catch (IOException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    
    
    }
}
