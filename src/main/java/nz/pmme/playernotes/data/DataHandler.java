package nz.pmme.playernotes.data;


import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class DataHandler
{
    private static final int thisVersion = 1;
    private Plugin plugin;
    private Database database;

    // This map stores the query results from players.
    // Each player has only a single buffer of query results, i.e. the results of their last query.
    private Map< UUID, ArrayList<PlayerNote> > queryResultsForPlayers = new HashMap<>();

    public DataHandler( Plugin plugin, Database database ) {
        this.plugin = plugin;
        this.database = database;
    }

    public ArrayList<PlayerNote> getLastResults( UUID requestingPlayer )
    {
        return queryResultsForPlayers.get(requestingPlayer);
    }

    public void generateTables()
    {
        Connection connection = this.database.getConnection();
        try {
            PreparedStatement preparedStatement1 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS player_notes_other(id INTEGER PRIMARY KEY,key VARCHAR(255) NOT NULL,value VARCHAR(255) NOT NULL)");
            preparedStatement1.execute();
            preparedStatement1.close();

            PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS player_notes(id INTEGER PRIMARY KEY,player VARCHAR(255) NOT NULL,note_by VARCHAR(255) NOT NULL,note VARCHAR(255) NOT NULL)");
            preparedStatement.execute();
            preparedStatement.close();

            PreparedStatement preparedStatement2 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS player_notes_last(player VARCHAR(40) NOT NULL PRIMARY KEY,noteId INTEGER NOT NULL)");
            preparedStatement2.execute();
            preparedStatement2.close();
        }
        catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
    }

    public boolean checkVersion()
    {
        boolean versionOkay = true;
        Connection connection = this.database.getConnection();
        try {
            int version = 0;
            String statement = "SELECT value FROM player_notes_other WHERE key='VERSION'";
            PreparedStatement preparedStatement = connection.prepareStatement( statement );
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                String value = resultSet.getString("value");
                version = Integer.valueOf(value);
            }
            resultSet.close();
            preparedStatement.close();

            if( version == 0 )
            {
                // Set the first version number.
                preparedStatement = connection.prepareStatement( " INSERT INTO player_notes_other(key, value) VALUES ('VERSION','" + thisVersion + "')" );
                preparedStatement.execute();
                preparedStatement.close();
            }
            else if( version > thisVersion )
            {
                System.out.println( "[PlayerNotes] Database is a future version: " + version + ". This build uses version " + thisVersion + ". This may cause errors.");
                versionOkay = false;
            }
            else if( version < thisVersion )
            {
                System.out.println( "[PlayerNotes] Database version " + version + " will be upgraded to " + thisVersion + ".");

                // Upgrade the table structure to this version.

                // Update version number.
                preparedStatement = connection.prepareStatement( "UPDATE player_notes_other SET value='" + thisVersion + "' WHERE key='VERSION'" );
                preparedStatement.execute();
                preparedStatement.close();
            }
        }
        catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
        return versionOkay;
    }

    public void createNote( UUID requestingPlayer, String notingPlayer, String aboutPlayer, String note )
    {
        Connection connection = this.database.getConnection();
        try {
            String statement = "INSERT INTO player_notes(player, note_by, note) VALUES (?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement( statement );
            preparedStatement.setString( 1, aboutPlayer );
            preparedStatement.setString( 2, notingPlayer );
            preparedStatement.setString( 3, note );
            preparedStatement.execute();
            preparedStatement.close();

            // Clear any previous query results that may have been stored.
            queryResultsForPlayers.put( requestingPlayer, null );
        }
        catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
    }

    public ArrayList<PlayerNote> viewNotes( UUID requestingPlayer, String aboutPlayerFilter, String authorFilter )
    {
        Connection connection = this.database.getConnection();
        try {
            ArrayList<PlayerNote> results = new ArrayList<>();
            StringBuilder statement = new StringBuilder();
            statement.append( "SELECT * FROM player_notes" );
            if( aboutPlayerFilter != null ) {
                statement.append( " WHERE UPPER(player)='" ).append( aboutPlayerFilter.toUpperCase() ).append( "'" );
            } else if( authorFilter != null ) {
                statement.append( " WHERE UPPER(note_by)='" ).append( authorFilter.toUpperCase() ).append( "'" );
            }
            statement.append( " ORDER BY id" );
            PreparedStatement preparedStatement = connection.prepareStatement( statement.toString() );
            ResultSet resultSet = preparedStatement.executeQuery();
            while( resultSet.next() ) {
                PlayerNote playerNote = new PlayerNote( resultSet.getInt("id"), resultSet.getString("note_by"), resultSet.getString("player"), resultSet.getString("note") );
                results.add(playerNote);
            }
            resultSet.close();
            preparedStatement.close();

            // Store the results in the map. Results can be re-displayed and other pages can be displayed from these buffered results.
            queryResultsForPlayers.put( requestingPlayer, results );

            return results;
        }
        catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
        return null;
    }

    public void deleteNotes( UUID requestingPlayer, int id, String aboutPlayerFilter )
    {
        Connection connection = this.database.getConnection();
        try {
            StringBuilder statement = new StringBuilder();
            statement.append( "DELETE FROM player_notes" );
            if(aboutPlayerFilter != null ) {
                statement.append( " WHERE UPPER(player)='" ).append( aboutPlayerFilter.toUpperCase() ).append( "'" );
            } else if( id > 0 ) {
                statement.append( " WHERE id=" ).append( id );
            }
            PreparedStatement preparedStatement = connection.prepareStatement( statement.toString() );
            preparedStatement.execute();
            preparedStatement.close();

            // Clear any previous query results that may have been stored.
            queryResultsForPlayers.put( requestingPlayer, null );
        }
        catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
    }

    public ArrayList<String> getNotedPlayers()
    {
        Connection connection = this.database.getConnection();
        try {
            ArrayList<String> results = new ArrayList<>();
            String statement = "SELECT DISTINCT player FROM player_notes ORDER BY player";
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            ResultSet resultSet = preparedStatement.executeQuery();
            while( resultSet.next() ) {
                results.add( resultSet.getString("player") );
            }
            resultSet.close();
            preparedStatement.close();
            return results;
        }
        catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
        return null;
    }

    public ArrayList<String> getAuthors()
    {
        Connection connection = this.database.getConnection();
        try {
            ArrayList<String> results = new ArrayList<>();
            String statement = "SELECT DISTINCT note_by FROM player_notes ORDER BY note_by";
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            ResultSet resultSet = preparedStatement.executeQuery();
            while( resultSet.next() ) {
                results.add( resultSet.getString("note_by") );
            }
            resultSet.close();
            preparedStatement.close();
            return results;
        }
        catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
        return null;
    }

    public void saveLastNoteId(UUID player)
    {
        Connection connection = this.database.getConnection();
        try {
            String statement = "SELECT id FROM player_notes ORDER BY id DESC LIMIT 1";
            PreparedStatement preparedStatement = connection.prepareStatement( statement );
            ResultSet resultSet = preparedStatement.executeQuery();
            int lastId = resultSet.next() ? resultSet.getInt("id") : 0;
            preparedStatement.close();
            if(lastId > 0) {
                String statement2 = "REPLACE INTO player_notes_last(player, noteId) VALUES (?,?)";
                PreparedStatement preparedStatement2 = connection.prepareStatement( statement2 );
                preparedStatement2.setString( 1, player.toString() );
                preparedStatement2.setInt( 2, lastId );
                preparedStatement2.execute();
                preparedStatement2.close();
            }
        }
        catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
    }

    public boolean hasUnreadNotes(UUID player)
    {
        boolean playerHasUnreadNotes = false;
        Connection connection = this.database.getConnection();
        try {
            String statement = "SELECT id FROM player_notes ORDER BY id DESC LIMIT 1";
            PreparedStatement preparedStatement = connection.prepareStatement( statement );
            ResultSet resultSet = preparedStatement.executeQuery();
            int lastId = resultSet.next() ? resultSet.getInt("id") : 0;
            preparedStatement.close();
            if(lastId > 0) {
                String statement2 = "SELECT noteId FROM player_notes_last WHERE player='" + player.toString() + "'";
                PreparedStatement preparedStatement2 = connection.prepareStatement( statement2 );
                ResultSet resultSet2 = preparedStatement2.executeQuery();
                if(resultSet2.next()) {
                    if(resultSet2.getInt("noteId") < lastId) playerHasUnreadNotes = true;
                } else {
                    playerHasUnreadNotes = true;
                }
                preparedStatement2.close();
            }
        }
        catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
        return playerHasUnreadNotes;
    }

//    private static void asyncExecute(final PreparedStatement preparedStatement) {
//        new BukkitRunnable(){
//
//            public void run() {
//                try {
//                    preparedStatement.execute();
//                }
//                catch (SQLException sQLException) {
//                    sQLException.printStackTrace();
//                }
//            }
//        }.runTaskAsynchronously(plugin);
//    }
//
//    private static void asyncExecuteBatch(final PreparedStatement preparedStatement) {
//        new BukkitRunnable(){
//
//            public void run() {
//                try {
//                    preparedStatement.executeBatch();
//                }
//                catch (SQLException sQLException) {
//                    sQLException.printStackTrace();
//                }
//            }
//        }.runTaskAsynchronously(plugin);
//    }
}

