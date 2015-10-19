package client;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.Serializable;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;


public class Message implements Serializable{

    private static final long serialVersionUID = 1L;
    
    String message;
    Key publickey = null;
    boolean iskey = false;
    boolean isname = false;
    boolean isgamelist = false;
    byte[] encryptedmessage;
    List<String> gamelist = null;

    public boolean isIsgamelist() {
        return isgamelist;
    }

    public void setIsgamelist(boolean isgamelist) {
        this.isgamelist = isgamelist;
    }

    
    
    public List<String> getGamelist() {
        return gamelist;
    }

    public void setGamelist(List<String> gamelist) {
        this.gamelist = gamelist;
    }


    public byte[] getEncryptedmessage() {
        return encryptedmessage;
    }

    public void setEncryptedmessage(byte[] encryptedmessage) {
        this.encryptedmessage = encryptedmessage;
    }
    

    public Message(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Key getPublickey() {
        return publickey;
    }

    public void setPublickey(Key publickey) {
        this.publickey = publickey;
    }

    public boolean isIskey() {
        return iskey;
    }

    public void setIskey(boolean iskey) {
        this.iskey = iskey;
    }

    public boolean isIsname() {
        return isname;
    }

    public void setIsname(boolean isname) {
        this.isname = isname;
    }
    
    public void setByteMessage(byte[] encmessage){
        
        int i = encmessage.length;
        this.encryptedmessage = new byte[i];
        copybytes(encmessage);

    }
    
    public void copybytes(byte[] a){
    
        for(int i = 0;i<a.length;i++){
    
            this.encryptedmessage[i] = a[i];
    
        }
    
    }
    
}
