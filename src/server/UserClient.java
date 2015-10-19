package server;


import database.User;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class UserClient {
    
    private Integer userid = -1;
    private String username;
    //private Socket clientsocket;
    private SSLSocket clientsocket;
    private ClientListener commthread;
    private boolean authenticated = false;
    private User loggedinuser = null;
    
    ObjectOutputStream o;

    public UserClient(Integer userid, String username, SSLSocket clientsocket,ClientListener comthread) {
        try {
            this.userid = userid;
            this.username = username;
            this.clientsocket = clientsocket;
            this.commthread = comthread;
            
            this.o = new ObjectOutputStream(this.clientsocket.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(UserClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ObjectOutputStream getOutputStream() {
        return o;
    }
    
    public Integer getUserid() {
        return userid;
    }

    public String getUsername() {
        return username;
    }

    public Socket getClientsocket() {
        return clientsocket;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public ClientListener getCommthread() {
        return commthread;
    }

    public User getLoggedinuser() {
        return loggedinuser;
    }

    public void setLoggedinuser(User loggedinuser) {
        this.loggedinuser = loggedinuser;
    }

    

    
}
