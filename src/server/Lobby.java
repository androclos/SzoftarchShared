package server;


import client.Message;
import database.Database;
import database.DatabaseConnectionFactory;
import database.User;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOException;
import javax.net.ssl.SSLSocket;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Pifko
 */
public class Lobby implements Runnable{
    
    ArrayBlockingQueue<Message> messageque;

    Map<Integer,UserClient> userclients = new ConcurrentHashMap<Integer, UserClient>(); // cliensekt user objectekkel
    Map<Integer,UserClient> loggedinuserclients = new ConcurrentHashMap<Integer, UserClient>(); // cliensekt user objectekkel
    
    Map<Integer,Game> gamelist = new ConcurrentHashMap<Integer, Game>();
    Map<Integer,ArrayBlockingQueue<Message>> gamequelist = new HashMap<Integer,ArrayBlockingQueue<Message>>();
    Map<Integer,Game> usertogame = new ConcurrentHashMap<Integer,Game>();
    
    Database db;
    
    private Integer gameids = 0;
    
    public Lobby(ArrayBlockingQueue<Message> messageque) throws IOException, SQLException {
        this.messageque = messageque;
        this.db = DatabaseConnectionFactory.GetDatabaseConnection();
        gameids = db.getMaxGameId();
        
    }

    
    public void addsocket(SSLSocket socket, int name,ClientListener commthread){ //uj kapcsolat jott be
    
        ObjectOutputStream o;
        
        try {
            
            UserClient newuser = new UserClient(name, null, socket, commthread);
            userclients.put(name, newuser);

            Message namemsg = new Message(String.valueOf(name));
            newuser.getOutputStream().writeObject(namemsg);

            System.out.println("Sending name " + name + " to " + socket.toString() + "." );
            
        } catch (IOException ex) {
            Logger.getLogger(Lobby.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
    
    
    @Override
    public void run() {
        
        while(true){
        
            try {

                Message newmsg = this.messageque.take();
                List<String> msgparts = msgprocess(newmsg.getMessage());
                this.msghandler(msgparts,newmsg);
 
            } catch (InterruptedException ex) {
                Logger.getLogger(Lobby.class.getName()).log(Level.SEVERE, null, ex);
            } 
        
        }
        
    }
    
    
    public void userauth(List<String> msgparts){
    
        Integer clientid = Integer.valueOf(msgparts.get(0));
        String username = msgparts.get(2);
        String password = msgparts.get(3);
    
        ObjectOutputStream o;
        User u = null;
        try{
        
            u = db.Login(username,password);
            
            if(u != null){
                
                UserClient newloggedinuser = userclients.get(clientid);
                newloggedinuser.setUsername(username);
                newloggedinuser.setAuthenticated(true);
                newloggedinuser.setLoggedinuser(u);
                loggedinuserclients.put(clientid, newloggedinuser);

                
                synchronized(loggedinuserclients.get(clientid).getClientsocket().getOutputStream()){
                        
                    Message newmsg = new Message("Succesful login.");
                    loggedinuserclients.get(clientid).getOutputStream().writeObject(newmsg);
                    System.out.println("Succesful login from: " + clientid +" as " + username +".");
                    
                }
            }
            else{
                
                synchronized(userclients.get(clientid).getClientsocket().getOutputStream()){
                        
                    Message newmsg = new Message("Login failed.");
                    userclients.get(clientid).getOutputStream().writeObject(newmsg);
                    System.out.println("Failed login from: " + clientid +".");
                }
                
            }
        }
        catch(IOException ex){
             Logger.getLogger(Lobby.class.getName()).log(Level.SEVERE, null, ex);    
        } catch (SQLException ex) {
            Logger.getLogger(Lobby.class.getName()).log(Level.SEVERE, null, ex);
        }
                    
    } 

    public List<String> msgprocess(String s){
    
        StringTokenizer st = new StringTokenizer(s,":"); //a:b:b:c uzenete felosztasa
        List<String> msgparts = new ArrayList<String>();      
        int partnum = st.countTokens();
                
        for(int i = 0;i<partnum;i++){
            msgparts.add(st.nextToken());
        }
    
        return msgparts;
    }   

    
    public void msghandler(List<String> message, Message original){
    
        Integer clientid = Integer.valueOf(message.get(0));
        String messageoperation = message.get(1);
        
        switch(messageoperation){
        
            case "auth":{
                this.userauth(message);
                break;
            }
            case "logout":{
                this.logout(clientid);
                if(usertogame.containsKey(clientid))
                    usertogame.get(clientid).addmessage(original);
                break;
            }
            case "newgame":{
                this.startnewgame(clientid);
                break;
            }
            case "getgamelist":{
                break;
            }
            case "joingame":{
                Integer gametojonid = Integer.valueOf(message.get(2));
                this.joingame(clientid, gametojonid);
                break;
            }
            case "move":{
                usertogame.get(clientid).addmessage(original);
                break;
            }
            
            case "leavegame":{
                usertogame.get(clientid).addmessage(original);
                this.leavegame(clientid);
                //usertogame.get(clientid).addmessage(original);
                break;
            }
            case "gamelist":{
                this.sendGameList(clientid);
                break;
            }
            case "closeclient":{
                closeclient(clientid);
                break;
            }
            default: {
                this.messagetoclient(clientid, "message:Error, command does not exist.");
                break;
            }
        }

    }
    
    public void startnewgame(Integer clientid){
    
        this.gameids++;
        ArrayBlockingQueue<Message> newgameque = new ArrayBlockingQueue<Message>(100);
        Game newgame = new Game(newgameque,this.gameids,this,false);

        newgame.addplayer(loggedinuserclients.get(clientid));
        this.gamelist.put(gameids, newgame);
        this.gamequelist.put(gameids, newgameque);
        this.usertogame.put(clientid, newgame);
                
        new Thread(newgame).start();
    
    }
    
    public void joingame(Integer clientid, Integer gameid){
    
        this.gamelist.get(gameid).addplayer(this.loggedinuserclients.get(clientid));
        this.usertogame.put(clientid, this.gamelist.get(gameid));
    
    }
    
    public void logout(Integer userclientid){
    
        loggedinuserclients.remove(userclientid);
        userclients.get(userclientid).setLoggedinuser(null);
        userclients.get(userclientid).setAuthenticated(false);
        if(usertogame.keySet().contains(userclientid)){
            leavegame(userclientid);
            usertogame.remove(userclientid);
        }

    }
    
    public void closeclient(Integer id){
    
        logout(id);
        userclients.get(id).getCommthread().stopThread();
        userclients.remove(id);
        System.out.println("Client: "+id+" ,closed the connection.");

    }
    
    public void gamemessage(Integer gameid,Message message){
        
        this.gamequelist.get(gameid).add(message);
    
    }
    
    public void messagetoclient(Integer clientid,String message){
    
        try {

            ObjectOutputStream o = userclients.get(clientid).getOutputStream();
            Message msg = new Message(message);
            o.writeObject(msg);
        } catch (IOException ex) {
            Logger.getLogger(Lobby.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void leavegame(Integer id){

        //this.usertogame.get(id).playerleaving(id);
        this.usertogame.remove(id);
    }

    public void sendGameList(Integer id){
    
        try {
            ArrayList<String> listofgames = new ArrayList<String>();
            String user = this.loggedinuserclients.get(id).getUsername();

            for (Map.Entry<Integer, Game> entry : this.gamelist.entrySet()){

                if(entry.getValue().ableToJoin(id))
                //if(entry.getValue().ableToJoin(user))
                    //listofgames.add(entry.getValue().getGameid()+":"+entry.getValue().getotherplayer(user));
                    listofgames.add(entry.getValue().getGameid()+":"+entry.getValue().getotherplayer(id));

            }

            List<String> usersgamelist = db.getUsersGameList(id);
            
            for(String s : usersgamelist)
                listofgames.add(s);
            
            Message m = new Message("Game list.");
            m.setIsgamelist(true);
            m.setGamelist(listofgames);
            this.loggedinuserclients.get(id).getOutputStream().writeObject(m);
            
        } catch (IOException ex) {
            Logger.getLogger(Lobby.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Lobby.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    
    }
    
    public void gameneded(Integer gid){
    
        
    
    }
    
    public Database getDatabaseAccess() throws IOException{
    
        return DatabaseConnectionFactory.GetDatabaseConnection();
    
    }

}