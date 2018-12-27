package nz.pmme.playernotes;

import nz.pmme.playernotes.commands.Commands;
import nz.pmme.playernotes.data.Database;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerNotes extends JavaPlugin {
    private Database database = new Database(this);

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        database.openDatabaseConnection();
        this.getCommand("pn").setExecutor(new Commands(this));
    }

    @Override
    public void onDisable() {
        database.closeConnection();
    }

    public Database getDatabase() {
        return database;
    }
}

