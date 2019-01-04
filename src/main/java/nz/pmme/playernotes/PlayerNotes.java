package nz.pmme.playernotes;

import nz.pmme.playernotes.commands.Commands;
import nz.pmme.playernotes.data.DataHandler;
import nz.pmme.playernotes.data.Database;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerNotes extends JavaPlugin {
    private Database database = new Database(this);
    private DataHandler dataHandler = new DataHandler(this, this.database);

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        database.openDatabaseConnection();
        dataHandler.generateTables();
        dataHandler.checkVersion();
        this.getCommand("pn").setExecutor(new Commands(this));
    }

    @Override
    public void onDisable() {
        database.closeConnection();
    }

    public Database getDatabase() {
        return database;
    }
    public DataHandler getDataHandler() { return dataHandler; }
}

