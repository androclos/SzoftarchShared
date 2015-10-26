package server;





import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PublicKey;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class ENDEcryptorRSA {
   

    private Key publicKey = null;
    private Key privateKey = null;
    private Key foreginpublicKey = null;
    private Cipher cipher = null;


    public ENDEcryptorRSA() {
        
        try {
            
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048 * 2);
            KeyPair kp = kpg.genKeyPair();
           
            this.publicKey = kp.getPublic();
            this.privateKey = kp.getPrivate();
            this.cipher = Cipher.getInstance("RSA");

               
                        
        } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(ENDEcryptorRSA.class.getName()).log(Level.SEVERE, null, ex);
        }
    }



    
    public byte[] encryptmessage(String message){
        
        try {
            
            byte[] data = message.getBytes();
            cipher.init(Cipher.ENCRYPT_MODE, this.privateKey);
            byte[] result = cipher.doFinal(data);
            //return new String(result);
            return result;
            
        } catch (InvalidKeyException ex) {
            Logger.getLogger(ENDEcryptorRSA.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(ENDEcryptorRSA.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(ENDEcryptorRSA.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return new byte[1];

    }
    public String decryptmessage(){return "";}
    public String decryptforeginmessage(byte[] message,Key key){
        
        try {
            byte[] data = message;
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(data);
            return new String(result);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(ENDEcryptorRSA.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(ENDEcryptorRSA.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(ENDEcryptorRSA.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return "";
        
        
    }

    public Key getForeginpublicKey() {
        return foreginpublicKey;
    }

    public void setForeginpublicKey(String key) {
        
        try {
            
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(key.getBytes());
            KeyFactory keyFact = KeyFactory.getInstance("RSA");
            this.foreginpublicKey = keyFact.generatePublic(x509KeySpec);
            System.out.println(this.foreginpublicKey.getEncoded().toString());
            
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ENDEcryptorRSA.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(ENDEcryptorRSA.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
}
