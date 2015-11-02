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

            //Message namemsg = new Message(String.valueOf(name));
            Message namemsg = new Message("name:" + String.valueOf(name));
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
                        
                    Message newmsg1 = new Message("message:Succesful login.");
                    Message newmsg2 = new Message("login:success");
                    loggedinuserclients.get(clientid).getOutputStream().writeObject(newmsg1);
                    loggedinuserclients.get(clientid).getOutputStream().writeObject(newmsg2);
                    System.out.println("Succesful login from: " + clientid +" as " + username +".");
                    
                }
            }
            else{
                
                synchronized(userclients.get(clientid).getClientsocket().getOutputStream()){
                        
                    Message newmsg1 = new Message("message:Login failed.");
                    Message newmsg2 = new Message("login:failure");
                    userclients.get(clientid).getOutputStream().writeObject(newmsg1);
                    userclients.get(clientid).getOutputStream().writeObject(newmsg2);
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
            case "joingame":{
                Integer gametojonid = Integer.valueOf(message.get(2));
                this.joingame(clientid, gametojonid);
                break;
            }
            case "loadgame":{
                Integer gametojonid = Integer.valueOf(message.get(2));
                startnewloadedgame(clientid, gametojonid, original);
                break;
            }
            case "move":{
                usertogame.get(clientid).addmessage(original);
                break;
            }
            
            case "leavegame":{
                usertogame.get(clientid).addmessage(original);
                this.leavegame(clientid);
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
    
    public void startnewloadedgame(Integer clientid, Integer gameid, Message original){
    
        ArrayBlockingQueue<Message> newgameque = new ArrayBlockingQueue<Message>(100);
        Game newgame = new Game(newgameque,gameid,this,true);
        new Thread(newgame).start();
        newgame.addmessage(original);

        newgame.addplayer(loggedinuserclients.get(clientid));
        this.gamelist.put(gameids, newgame);
        this.gamequelist.put(gameids, newgameque);
        this.usertogame.put(clientid, newgame);

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
    
    public void closeclient(Integer clientid){
    
        logout(clientid);
        userclients.get(clientid).getCommthread().stopThread();
        userclients.remove(clientid);
        System.out.println("Client: "+clientid+" ,closed the connection.");

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

        this.usertogame.remove(id);
    }

    public void sendGameList(Integer clientid){ //adatbazisbol vissza adja azokat a jatekokat amik lehet hogy be vannak toltve
    
        try {
            
            Integer userid = loggedinuserclients.get(clientid).getLoggedinuser().getUserid(); //user ideja nem a cliense
            
            ArrayList<String> listofongoinggames = new ArrayList<String>();
            ArrayList<Integer> ongoinggameids = new ArrayList<Integer>();
            for (Map.Entry<Integer, Game> entry : this.gamelist.entrySet()){

                if(entry.getValue().ableToJoin(userid)){
                    listofongoinggames.add(entry.getValue().getGameid()+":"+entry.getValue().getotherplayer(userid));
                    ongoinggameids.add(entry.getValue().getGameid());
                }
            }

            Map<Integer, String> listofloadablegames = db.getUsersGameList(userid);
            
            /*for(String s : listofloadablegames)
                listofongoinggames.add(s);*/
            
            for (Map.Entry<Integer, String> entry : listofloadablegames.entrySet()){

                if(!ongoinggameids.contains(entry.getKey())){
                    listofongoinggames.add(entry.getValue());
                }
            }

            Message m = new Message("gamelist:Game list.");
            m.setIsgamelist(true);
            m.setGamelist(listofongoinggames);
            this.loggedinuserclients.get(clientid).getOutputStream().writeObject(m);
            
        } catch (IOException ex) {
            Logger.getLogger(Lobby.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Lobby.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void gameneded(Integer gid){
    
        gamelist.remove(gid);
    
    }
    
    public Database getDatabaseAccess() throws IOException{
    
        return DatabaseConnectionFactory.GetDatabaseConnection();
    
    }

}