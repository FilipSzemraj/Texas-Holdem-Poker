package sql;

import org.main.Main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {
    public Connection databaseLink;

    public Connection getConnection(){
        String databaseName = "db_poker";
        String databaseUser = "Filip";
        String databasePassword = "1234";
        String location = ("C:/Projekty_JAVA/Texas_holdem/Texas_holdem/src/main/resources/DB_POKER.db");
        String dbPrefix = "jdbc:sqlite:";

        try{
            databaseLink = DriverManager.getConnection(dbPrefix + location);
        }catch(SQLException exception)
        {
            Logger.getAnonymousLogger().log(Level.SEVERE, LocalDateTime.now() + ": Nie udalo sie polaczyc z SQLite DB w " + location);
            return null;
        }

        return databaseLink;

    }


}
