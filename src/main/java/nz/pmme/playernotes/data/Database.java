package nz.pmme.playernotes.data;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class Database {
    private Connection connection;
    private Plugin plugin;

    public Database( Plugin plugin ) {
        this.plugin = plugin;
    }

    public void openDatabaseConnection()
    {
        FileConfiguration config = plugin.getConfig();
        try {
            if (config.getBoolean("SQLite")) {
                try {
                    Class.forName("org.sqlite.JDBC");
                }
                catch (ClassNotFoundException classNotFoundException) {
                    classNotFoundException.printStackTrace();
                }
                File file = new File(String.valueOf(plugin.getDataFolder().toString()) + File.separator + "playernotes.db");
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    }
                    catch (IOException iOException) {
                        iOException.printStackTrace();
                    }
                }
                connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            } else {
                connection = DriverManager.getConnection("jdbc:mysql://" + config.getString("sql.host") + ":" + config.getString("sql.port") + "/" + config.getString("sql.database"), config.getString("sql.user"), config.getString("sql.pass"));
            }
        }
        catch (SQLException sQLException) {
            System.out.println("ERROR CONNECTING TO DATABASE!");
            sQLException.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            connection.close();
        }
        catch (SQLException sQLException) {
            System.out.println("COULD NOT CLOSE CONNECTION!");
            sQLException.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                this.openDatabaseConnection();
            }
        }
        catch (SQLException sQLException) {
            // empty catch block
        }
        return connection;
    }
}

