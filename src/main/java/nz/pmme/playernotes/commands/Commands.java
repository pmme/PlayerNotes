package nz.pmme.playernotes.commands;

import nz.pmme.playernotes.PlayerNotes;
import nz.pmme.playernotes.data.PlayerNote;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Commands implements CommandExecutor, TabCompleter {
    private PlayerNotes plugin;
    private static final String[] firstArguments = {
            "create",
            "new",
            "view",
            "viewall",
            "del",
            "delplayer",
            "delall"
    };
    private static final String[] msgPlayerNotesUsage = {
            ChatColor.DARK_AQUA + "PlayerNotes command usage:",
            ChatColor.WHITE + "/playernotes create <player> <notes>" + ChatColor.DARK_AQUA + " - Create note about player.",
            ChatColor.WHITE + "/playernotes new <player> <notes>" + ChatColor.DARK_AQUA + " - Create note about player.",
            ChatColor.WHITE + "/playernotes view <player>" + ChatColor.DARK_AQUA + " - View notes about player.",
            ChatColor.WHITE + "/playernotes viewall" + ChatColor.DARK_AQUA + " - View all notes.",
            ChatColor.WHITE + "/playernotes <page number>" + ChatColor.DARK_AQUA + " - View page of notes from last view/viewall.",
            ChatColor.WHITE + "/playernotes del <note id>" + ChatColor.DARK_AQUA + " - Delete notes.",
            ChatColor.WHITE + "/playernotes delplayer <player>" + ChatColor.DARK_AQUA + " - Delete all notes about player.",
            ChatColor.WHITE + "/playernotes delall" + ChatColor.DARK_AQUA + " - Delete all player notes. Empty the database."
    };
    private static final String msgPlayerNotesNoConsole = "This pn command must be used by an active player.";
    private static final String msgNoPermission = ChatColor.RED + "You do not have permission to use this command.";

    private static int resultsPerPage = 18;

    private final UUID uuidForConsole = UUID.randomUUID();  // A random UUID to represent the console when queries are entered from the console.

    public Commands(PlayerNotes plugin) {
        this.plugin = plugin;
    }

    private UUID getUuidForSender( CommandSender sender ) {
        return ( sender instanceof Player) ? ((Player) sender).getUniqueId() : this.uuidForConsole;
    }

    String getFormattedNoteLine( PlayerNote playerNote ) {
        return ChatColor.GRAY + "#" + playerNote.id + ": " + ChatColor.DARK_AQUA + playerNote.player + ChatColor.GRAY + " - " + ChatColor.WHITE + playerNote.note + ChatColor.RED + " < " + playerNote.notingPlayer;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String alias, String[] args)
    {
        if( commandSender.hasPermission("playernotes.view") ) {
            if(args.length == 1)
            {
                List<String> matchingFirstArguments = new ArrayList<>();
                String arg0lower = args[0].toLowerCase();
                for(String argument : firstArguments) {
                    if(arg0lower.isEmpty() || argument.toLowerCase().startsWith(arg0lower)) {
                        matchingFirstArguments.add(argument);
                    }
                }
                return matchingFirstArguments;
            }
            else if(args.length == 2)
            {
                if( args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("new") )
                {
                    List<String> matchingPlayerNames = new ArrayList<>();
                    String arg1lower = args[1].toLowerCase();
                    for(Player player : plugin.getServer().getOnlinePlayers()) {
                        if(arg1lower.isEmpty() || player.getName().toLowerCase().startsWith(arg1lower) || ChatColor.stripColor(player.getDisplayName()).toLowerCase().startsWith(arg1lower)) {
                            matchingPlayerNames.add(player.getName());
                        }
                    }
                    return matchingPlayerNames;
                }
                else if( args[0].equalsIgnoreCase("view") || args[0].equalsIgnoreCase("delplayer") )
                {
                    return plugin.getDataHandler().getNotedPlayers();
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        if( !sender.hasPermission("playernotes.view") ) {
            sender.sendMessage(msgNoPermission);
            return true;
        }
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
            case "new":
                if( !sender.hasPermission("playernotes.create") ) {
                    sender.sendMessage(msgNoPermission);
                    return true;
                }
                if( args.length >= 3 ) {
                    StringBuilder note = new StringBuilder();
                    for( int i = 2; i < args.length; ++i ) note.append( args[i] ).append(" ");
                    if( sender instanceof Player ) {
                        plugin.getDataHandler().createNote( this.getUuidForSender(sender), /*((Player)sender).getUniqueId().toString()*/sender.getName(), args[1], note.toString() );
                    } else {
                        plugin.getDataHandler().createNote( this.getUuidForSender(sender),"Console", args[1], note.toString() );
                    }
                    sender.sendMessage(ChatColor.GREEN + "Note added");
                }
                else displayCommandUsage(sender);
                return true;
            case "view":
                if( !sender.hasPermission("playernotes.view") ) {
                    sender.sendMessage(msgNoPermission);
                    return true;
                }
                if( args.length == 2 ) {
                    ArrayList<PlayerNote> results = plugin.getDataHandler().viewNotes( this.getUuidForSender(sender), args[1] );
                    if( results.isEmpty() ) {
                        sender.sendMessage(ChatColor.GRAY + "There are no player notes for " + args[1]);
                    } else {
                        int pagesOfResults = ( results.size() / this.resultsPerPage ) + 1;
                        sender.sendMessage(ChatColor.GRAY + "Player notes for " + args[1] + ":" );
                        for( int i = 0, line = 0; i < results.size() && line < this.resultsPerPage; ++i, ++line ) {
                            PlayerNote playerNote = results.get(i);
                            sender.sendMessage(this.getFormattedNoteLine(playerNote));
                        }
                        sender.sendMessage( ChatColor.GRAY + "Page 1 of " + pagesOfResults + " notes for " + args[1] );
                    }
                }
                else displayCommandUsage(sender);
                return true;
            case "viewall":
                if( !sender.hasPermission("playernotes.view") ) {
                    sender.sendMessage(msgNoPermission);
                    return true;
                }
                ArrayList<PlayerNote> results = plugin.getDataHandler().viewNotes( this.getUuidForSender(sender),null );
                if( results.isEmpty() ) {
                    sender.sendMessage(ChatColor.GRAY + "There are no player notes" );
                } else {
                    int pagesOfResults = ( results.size() / this.resultsPerPage ) + 1;
                    sender.sendMessage(ChatColor.GRAY + "All player notes:" );
                    for( int i = 0, line = 0; i < results.size() && line < this.resultsPerPage; ++i, ++line ) {
                        PlayerNote playerNote = results.get(i);
                        sender.sendMessage(this.getFormattedNoteLine(playerNote));
                    }
                    sender.sendMessage( ChatColor.GRAY + "Page 1 of " + pagesOfResults + " notes" );
                }
                return true;
            case "del":
                if( !sender.hasPermission("playernotes.delete") ) {
                    sender.sendMessage(msgNoPermission);
                    return true;
                }
                try {
                    if(args.length == 2 && Integer.valueOf(args[1]) > 0) {
                        plugin.getDataHandler().deleteNotes( this.getUuidForSender(sender), Integer.valueOf(args[1]), null );
                        sender.sendMessage(ChatColor.RED + "Note " + ChatColor.DARK_AQUA + args[1] + ChatColor.RED + " deleted");
                    } else displayCommandUsage(sender);
                } catch(NumberFormatException e) {
                    displayCommandUsage(sender);
                }
                return true;
            case "delplayer":
                if( !sender.hasPermission("playernotes.delete") ) {
                    sender.sendMessage(msgNoPermission);
                    return true;
                }
                if( args.length == 2 ) {
                    plugin.getDataHandler().deleteNotes( this.getUuidForSender(sender), 0, args[1] );
                    sender.sendMessage(ChatColor.RED + "Notes for player " + ChatColor.DARK_AQUA + args[1] + ChatColor.RED + " deleted");
                }
                else displayCommandUsage(sender);
                return true;
            case "delall":
                if( !sender.hasPermission("playernotes.deleteall") ) {
                    sender.sendMessage(msgNoPermission);
                    return true;
                }
                plugin.getDataHandler().deleteNotes( this.getUuidForSender(sender), 0, null );
                sender.sendMessage(ChatColor.RED + "All notes deleted!");
                return true;
            }

            // Check if the command was "/playernotes <page number>"
            if( Character.isDigit( args[0].charAt(0) ) )
            {
                ArrayList<PlayerNote> results = plugin.getDataHandler().getLastResults( this.getUuidForSender(sender) );
                if( results == null ) {
                    sender.sendMessage( ChatColor.GRAY + "You have no current query. Use " + ChatColor.WHITE + "/playernotes view" + ChatColor.GRAY + " to query notes." );
                } else {
                    int page = Integer.valueOf( args[0] );
                    int pagesOfResults = ( results.size() / this.resultsPerPage ) + 1;
                    if( page < 1 ) {
                        sender.sendMessage( ChatColor.GRAY + "Invalid page number. Page numbers start at 1." );
                    } else if( page > pagesOfResults ) {
                        sender.sendMessage( ChatColor.GRAY + "Page number is too high. There are " + pagesOfResults + " pages of notes." );
                    } else {
                        sender.sendMessage(ChatColor.GRAY + "Player notes:" );
                        for( int i = (page-1) * this.resultsPerPage, line = 0; i < results.size() && line < this.resultsPerPage; ++i, ++line ) {
                            PlayerNote playerNote = results.get(i);
                            sender.sendMessage(this.getFormattedNoteLine(playerNote));
                        }
                        sender.sendMessage( ChatColor.GRAY + "Page " + page + " of " + pagesOfResults + " notes" );
                    }
                }
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
