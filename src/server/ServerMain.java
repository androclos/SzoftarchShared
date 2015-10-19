package server;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Pifko
 */
public class ServerMain {
    
    
    public static void main(String[] args) {   
    
    Server server = new Server(2222);
    new Thread(server).start();

        
    }
    
}
