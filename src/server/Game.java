package server;


import chess.Cell;
import chess.ChessBoard;
import client.Message;
import database.ChessPiece;
import database.Database;
import java.io.IOException;
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
    private Map<Integer,String> fixedplayers = new HashMap<Integer,String>(); // egyszer mar csatalkozott jatekosok, userid
    private Map<String,String> playercolor;
    private Integer currentturnclientid; //aktualis jatekos cliensid-je nem userid
    private Integer currentturnuserid = -1;
    private Integer gameid; 
    private boolean gamestrated = false;
    private boolean gamehalted = false;
    private boolean loadedgame = false;
    private boolean movedone = false;
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
                sendmessage(u.getClientId(), m);
            } catch (IOException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        }
    
    }
    
    public void addPlayerToLoadedGame(UserClient newuser){
    
        try {
   
            
            if(players.size() < 1){
                
                players.add(newuser);
                Message msg = new Message("message:Waiting for player 2.");
                Message msgjoin = new Message("joined");
                sendmessage(newuser.getClientId(), msgjoin);
                sendmessage(newuser.getClientId(), msg);
                
            }
            else{

                players.add(newuser);
                for(UserClient u: players){
        
                    if(u.getLoggedinuser().getUserid().equals(currentturnuserid))
                    currentturnclientid = u.getClientId();
        
                }
                Message msg1 = new Message("message:Both player is present, game is started.");
                Message msgboard = new Message("boardstate:"+board.toString());
                Message msgwhite= new Message("color:white");
                Message msgblack= new Message("color:black");
                Message msgcurrentturn= new Message("game:turn:"+currentturnclientid);

                
                sendmessage(userbyname(playercolor.get("white")).getClientId(), msgwhite);
                sendmessage(userbyname(playercolor.get("black")).getClientId(), msgblack);

                broadcast(msgcurrentturn);
                broadcast(msgboard);
                broadcast(msg1);
                
            }
        } catch (IOException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
    
    public synchronized void addplayer(UserClient newuser){

        try{

            if(loadedgame == true){
                
                addPlayerToLoadedGame(newuser);
                return;
            
            }
            
            if(players.size() <1){
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                gamestarttime = dateFormat.format(new Date());
                
                
                players.add(newuser);
                if(fixedplayers.containsKey(newuser.getLoggedinuser().getUserid()) == false)
                    fixedplayers.put(newuser.getLoggedinuser().getUserid(),newuser.getUsername());
                    
                Message msg = new Message("message:Waiting for player 2.");
                Message msgjoin = new Message("joined");
                sendmessage(newuser.getClientId(), msgjoin);
                sendmessage(newuser.getClientId(), msg);
                
                System.out.println("Player: "+ this.players.get(0).getUsername() + " joind to game: " + this.gameid + ".");
                System.out.println("New game with id: "+this.gameid+", started by: "+this.players.get(0).getUsername()+ ".");
                
            }
            
            else{
                
                players.add(newuser);       
                if(fixedplayers.containsKey(newuser.getLoggedinuser().getUserid()) == false)
                    fixedplayers.put(newuser.getLoggedinuser().getUserid(),newuser.getUsername());
                
                this.gamestrated = true;
                
                newgameinit();
                
                System.out.println("Player: "+ this.players.get(1).getUsername() + " joind to game: " + this.gameid + ".");
                System.out.println("Game: "+this.gameid+", started with: "+this.players.get(0).getUsername() + " and " + this.players.get(1).getUsername()+ ".");
            }
            
        }catch (Exception ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void addmessage(Message s){
    
        this.gamemessageque.add(s);
        
    }
    
    @Override
    public void run() {
        
        while(true){
            try {

                Message newmsg = this.gamemessageque.take();
                
                if(newmsg.getMessage().equals("stopgame"))
                    return;
                
                System.out.println("Game message: "+newmsg.getMessage());
                List<String> message = msgprocess(newmsg);
                msghandler(message);

            } catch (InterruptedException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    public void playerleaving(Integer clientid){
    
        try {
            
            this.gamehalted = true;
            UserClient leavingplayer = this.userbyid(clientid);
            this.players.remove(this.userbyid(clientid));
   
            if(players.size() == 0 && fixedplayers.size() > 1){ //minden jatekos elment es volt lepes

                
                this.saveGameToDatabase();
                addmessage(new Message("stopgame"));
                lobby.gameneded(gameid);
                return;
            }



            Message m2 = new Message("stopgame");
            Message m3 = new Message("message:"+leavingplayer.getUsername() + " has left the game, game is halted.");
            for(UserClient u : players){

                sendmessage(u.getClientId(), m2);
                sendmessage(u.getClientId(), m3);
                
            }
 
        } catch (IOException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public UserClient userbyid(Integer clientid){
    
        UserClient player = null;
        for(UserClient u : this.players){
        
            if(u.getClientId().equals(clientid))
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
    
    public boolean ableToJoin(Integer userid){ //?? nem biztos hogy jo

        return fixedplayers.containsKey(userid) || (this.players.size()== 1 && fixedplayers.size() == 1);
    }
    
    public String getotherplayer(Integer userid){
        
        String username = "";
        for (Map.Entry<Integer, String> entry : this.fixedplayers.entrySet()){
            
            if(!entry.getKey().equals(userid))
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
                playerleaving(clientid);
                break;
            }
            case "logout":{
                playerleaving(clientid);
                break;
            }
            case "loadgame":{
                loadGameFromDatabase(Integer.valueOf(message.get(2)));
                break;
            }
            
            default:{
                break;
            }
        
        }
    
    }
    
    
    public void newgameinit() throws IOException{
    
        playercolor = new HashMap<String,String>();
        board = new ChessBoard();
        
        Random rand = new Random();
        if(rand.nextInt(2) == 1){
        
            playercolor.put("white", players.get(0).getUsername());
            playercolor.put("black", players.get(1).getUsername());
            currentturnclientid = players.get(0).getClientId();
            currentturnuserid = players.get(0).getLoggedinuser().getUserid();
        
        }
        else{
        
            playercolor.put("white", players.get(1).getUsername());
            playercolor.put("black", players.get(0).getUsername());
            currentturnclientid = players.get(1).getClientId();
            currentturnuserid = players.get(1).getLoggedinuser().getUserid();
            
        }
        
        Message gamestarted = new Message("gamestart");
        Message whitecolor = new Message("color:white");
        Message blackcolor = new Message("color:black");
        
        sendmessage(userbyname(playercolor.get("black")).getClientId(), blackcolor);
        sendmessage(userbyname(playercolor.get("white")).getClientId(), whitecolor);
        broadcast(gamestarted);
        
    }
    
    public void move(Integer clientid,Cell src, Cell dest){
    
        try{
            
            if(clientid != currentturnclientid){
                Message notyourturn = new Message("message:Not your turn.");
                sendmessage(clientid, notyourturn);
                return;
            }
            
            Integer outcome = board.move(src, dest, true);
            
            if(outcome == 1){
                
                movedone = true;
                
                for(UserClient client : players){
                
                    if(!client.getClientId().equals(clientid))
                        currentturnclientid = client.getClientId();
                        currentturnuserid = client.getLoggedinuser().getUserid();
                
                }
                
                Message succesfulmove = new Message("move:"+src.i+ ":"  + src.j+ ":"+dest.i +":"+dest.j); //move:1:2:3:4
                broadcast(succesfulmove);
            
            }
            else{
        
                Message wrongmove = new Message("message:Invalid move.");
                sendmessage(clientid, wrongmove);

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
    
    public void loadGameFromDatabase(Integer gameid){ //nincs kesz
    
        try {

            playercolor = new HashMap<String,String>();
            
            Database db = lobby.getDatabaseAccess();
            
            Map<String,String> gamedetails = db.getGameInformation(gameid);
            setGameDetails(gamedetails);
            
            List<ChessPiece> pieces = db.loadGame(gameid);
            board = new ChessBoard(pieces);
            
            if(gamedetails.get("whiteid").equals(gamedetails.get("currentid")))
                board.setWhiteOnTurn(true);
            else
                board.setWhiteOnTurn(false);
            
            System.out.println(board.toString());
            
            
        } catch (SQLException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }

    public void saveGameToDatabase(){
    
        try {

            Database db = lobby.getDatabaseAccess();
            if(db.isNewGame(gameid) == false){
                
                db.clearGamePieceList(gameid);
                db.saveBoard(gameid, board);
                db.updateGameState(gameid, currentturnuserid);
   
            }
            else{
            
                db.saveGame(getFixedPlayerId(playercolor.get("white")), getFixedPlayerId(playercolor.get("black")), currentturnuserid, gamestarttime, board);
            }
            
        } catch (IOException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }

    public void setGameDetails(Map<String,String> datamap){

        for (Map.Entry<String, String> entry : datamap.entrySet()){
            
            System.out.println(entry.getKey() +" : "+ entry.getValue());

        }
        
        fixedplayers.put(Integer.valueOf(datamap.get("blackid")), datamap.get("black"));
        fixedplayers.put(Integer.valueOf(datamap.get("whiteid")), datamap.get("white"));
        playercolor.put("black",datamap.get("black"));
        playercolor.put("white",datamap.get("white"));
        gamestarttime = datamap.get("startdate");
        

        currentturnuserid = Integer.valueOf(datamap.get("currentid"));
        
        for(UserClient u: players){
        
            if(u.getLoggedinuser().getUserid().equals(currentturnuserid))
                currentturnclientid = u.getClientId();
        
        }
        
    }
    
    public Integer getFixedPlayerId(String username){
    
        Integer id = -1;
        for (Map.Entry<Integer, String> entry : this.fixedplayers.entrySet()){
            
            if(entry.getValue().equals(username))
                id = entry.getKey();
        
        }
        return id;
    }
}
