/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Pifko
 */
@XmlRootElement
public class User implements Serializable{
    
    private Integer userid;
    private String name;

    
    public User() {
    }

    
    
    public User(Integer userid, String name) {
        this.userid = userid;
        this.name = name;
    }

    public Integer getUserid() {
        return userid;
    }

   
    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
    
}
