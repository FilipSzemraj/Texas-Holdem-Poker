package sql;

import org.main.Main;

import java.net.URL;
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
        URL databaseLocation = getClass().getResource("/database/DB_POKER.db");
        String dbPrefix = "jdbc:sqlite:";

        try{
            databaseLink = DriverManager.getConnection(dbPrefix + databaseLocation);
        }catch(SQLException exception)
        {
            Logger.getAnonymousLogger().log(Level.SEVERE, LocalDateTime.now() + ": Nie udalo sie polaczyc z SQLite DB w " + databaseLocation);
            return null;
        }

        return databaseLink;

    }


}
