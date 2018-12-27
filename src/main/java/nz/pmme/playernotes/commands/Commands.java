package nz.pmme.playernotes.commands;

import nz.pmme.playernotes.PlayerNotes;
import nz.pmme.playernotes.data.DataHandler;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Commands implements CommandExecutor
{
    private PlayerNotes plugin;
    private static final String[] msgPlayerNotesUsage = {
            ChatColor.DARK_AQUA + "PlayerNotes command usage:",
            ChatColor.WHITE + "/pn create <player> <notes>" + ChatColor.DARK_AQUA + " - Create notes about a player.",
            ChatColor.WHITE + "/pn view <player>" + ChatColor.DARK_AQUA + " - View notes about a player.",
            ChatColor.WHITE + "/pn viewall" + ChatColor.DARK_AQUA + " - View all notes.",
            ChatColor.WHITE + "/pn delete <note id>" + ChatColor.DARK_AQUA + " - Delete notes.",
            ChatColor.WHITE + "/pn deleteall" + ChatColor.DARK_AQUA + " - Delete all player notes. Empty the database."
    };
    private static final String msgPlayerNotesNoConsole = "This pn command must be used by an active player.";

    public Commands( PlayerNotes plugin ) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        if( args.length == 0 )
        {
            displayCommandUsage( sender );
            return true;
        }
        else if( args.length > 0 )
        {
            switch( args[0].toLowerCase() )
            {
            case "create":
                if( args.length >= 3 ) {
                    StringBuilder note = new StringBuilder();
                    for( int i = 2; i < args.length; ++i ) note.append( args[i] ).append(" ");
                    if( sender instanceof Player ) {
                        DataHandler.createNote( plugin.getDatabase(), /*((Player)sender).getUniqueId().toString()*/sender.getName(), args[1], note.toString() );
                    } else {
                        DataHandler.createNote( plugin.getDatabase(), "Console", args[1], note.toString() );
                    }
                    sender.sendMessage(ChatColor.GREEN + "Note added");
                }
                else displayCommandUsage(sender);
                return true;
            case "view":
                if( args.length == 2 ) {
                    ArrayList<String> results = DataHandler.viewNotes( plugin.getDatabase(), args[1] );
                    if( results.isEmpty() ) {
                        sender.sendMessage(ChatColor.GRAY + "There are no player notes for " + args[1]);
                    } else {
                        for( String result : results ) {
                            sender.sendMessage(result);
                        }
                        sender.sendMessage(ChatColor.GRAY + "End of notes for " + args[1]);
                    }
                }
                else displayCommandUsage(sender);
                return true;
            case "viewall":
                ArrayList<String> results = DataHandler.viewNotes( plugin.getDatabase(), null );
                if( results.isEmpty() ) {
                    sender.sendMessage(ChatColor.GRAY + "There are no player notes" );
                } else {
                    for( String result : results ) {
                        sender.sendMessage(result);
                    }
                    sender.sendMessage(ChatColor.GRAY + "End of notes" );
                }
                return true;
            case "delete":
                if( args.length == 2 ) {
                    DataHandler.deleteNote( plugin.getDatabase(), Integer.valueOf(args[1]) );
                    sender.sendMessage(ChatColor.RED + "Note deleted");
                }
                else displayCommandUsage(sender);
                return true;
            case "deleteall":
                DataHandler.deleteAllNotes( plugin.getDatabase() );
                sender.sendMessage(ChatColor.RED + "All notes deleted!");
                return true;
            }
        }
        displayCommandUsage( sender );
        return true;
    }

    protected void displayCommandUsage( CommandSender sender )
    {
        sender.sendMessage( msgPlayerNotesUsage );
    }

    protected void displayNoConsoleMessage( CommandSender sender )
    {
        sender.sendMessage( msgPlayerNotesNoConsole );
    }
}
