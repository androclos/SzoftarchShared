/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.io.IOException;

/**
 *
 * @author Pifko
 */
public class DatabaseConnectionFactory {
    
    public static Database GetDatabaseConnection() throws IOException{
    
        return new Database();
    
    }
    
}
