package server;


import client.Message;
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
    private SSLSocket clientsocket;
    private ClientListener commthread;
    private boolean authenticated = false;
    private User loggedinuser = null;
    
    ObjectOutputStream o;

    public UserClient(Integer userid, String username, SSLSocket clientsocket,ClientListener comthread) throws IOException {

            this.userid = userid;
            this.username = username;
            this.clientsocket = clientsocket;
            this.commthread = comthread;
            this.o = new ObjectOutputStream(this.clientsocket.getOutputStream());

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

    public void sendmessage(Message m) throws IOException{
    
        o.writeObject(m);
    
    }

    
}
