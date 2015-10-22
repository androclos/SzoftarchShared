package server;

import client.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ArrayBlockingQueue;
import javax.net.ssl.SSLSocket;


public class ClientListener implements Runnable{ //csak hallgat az adott porton es tovabbitja az uzeneteket egy que-ba

    protected SSLSocket clientSocket = null;
    protected ArrayBlockingQueue<Message> que = null;
    protected DataInputStream out;
    protected ObjectInputStream in;
    protected Integer clientid = -1;

    protected volatile boolean stopthread = false;

    public ClientListener(SSLSocket clientSocket, ArrayBlockingQueue<Message> a, Integer i) throws IOException {
        this.clientSocket = clientSocket;
        this.que = a;
        this.clientid = i;
        out =  new DataInputStream(clientSocket.getInputStream());
        in = new ObjectInputStream(clientSocket.getInputStream());

    }

    
    public void run() {

        String s = null;
        Message msg = null;
        try {
            while((msg = (Message)in.readObject()) != null){
            
                if(this.stopthread == true)
                    return;

                System.out.println("Message from client " + this.clientid + " : " + msg.getMessage() + ".");
                if(msg!=null && msg.getMessage()!=null){
                    que.add(msg);
                }
                
            }   
        } 
        catch (IOException ex) {
            Message poisonpill = new Message(this.clientid+":"+"closeclient");
            que.add(poisonpill);
            Logger.getLogger(ClientListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public synchronized void stopThread(){
    
        this.stopthread = true;
    
    }
}