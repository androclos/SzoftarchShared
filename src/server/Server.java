package server;

import client.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLServerSocket;
import java.security.Security;
import java.security.PrivilegedActionException;
import database.*;
import webservice.*;

import javax.net.ssl.*;
import com.sun.net.ssl.*;
import com.sun.net.ssl.internal.ssl.Provider;
import java.sql.SQLException;
import javax.xml.ws.Endpoint;

public class Server implements Runnable{

    protected int          serverPort   = 2222;
    //protected ServerSocket serverSocket = null;
    protected SSLServerSocket serverSocket = null;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;
    protected ExecutorService threadPool = Executors.newFixedThreadPool(30);
    protected int clientnames = 0;
    protected Database databaseconnection = null;

    protected Lobby lob;
    
    public ArrayBlockingQueue<Message> messageque = new ArrayBlockingQueue<Message>(100,true);
    
    public Server(int port){
        try {
            serverPort = port;
            
            Security.addProvider(new Provider());
            System.setProperty("javax.net.ssl.trustStoreType","JCEKS");
            System.setProperty("javax.net.ssl.keyStore","C:/temp/keystore.ks"); //kulcsot tartalmazza
            System.setProperty("javax.net.ssl.keyStorePassword","password"); //password hozza
            SSLServerSocketFactory sslServerSocketfactory = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
            serverSocket = (SSLServerSocket)sslServerSocketfactory.createServerSocket(serverPort);

            lob = new Lobby(messageque);

            databaseconnection = new Database();
            Endpoint.publish("http://localhost:7777/ws/user", new WebserviceImpl(DatabaseConnectionFactory.GetDatabaseConnection())); // web servcei publish
            
            new Thread(lob).start(); //lobby
            
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void run(){ //fogadja az uj connectionoket
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        
        while(! isStopped()){
            
            SSLSocket clientSocket = null;
            try {
                
                clientSocket = (SSLSocket)this.serverSocket.accept();
                clientnames++;
                
                ClientListener newclient = new ClientListener(clientSocket, messageque, clientnames);
                lob.addsocket(clientSocket, clientnames, newclient);
                this.threadPool.execute(newclient);

                System.out.println("Got connection with name: " + clientnames + ".");
                
            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Server Stopped.") ;
                    break;
                }
                throw new RuntimeException(
                    "Error accepting client connection", e);
            }

        }
        
        this.threadPool.shutdown();
        System.out.println("Server Stopped.") ;
    }


    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

}