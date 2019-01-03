package nz.pmme.playernotes.data;


import java.sql.*;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

public class DataHandler
{
    private static final int thisVersion = 1;

    public static void generateTables( Database database )
    {
        Connection connection = database.getConnection();
        try {
            PreparedStatement preparedStatement1 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS player_notes_other(id INTEGER PRIMARY KEY,key VARCHAR(255) NOT NULL,value VARCHAR(255) NOT NULL)");
            preparedStatement1.execute();
            preparedStatement1.close();

            PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS player_notes(id INTEGER PRIMARY KEY,player VARCHAR(255) NOT NULL,note_by VARCHAR(255) NOT NULL,note VARCHAR(255) NOT NULL)");
            preparedStatement.execute();
            preparedStatement.close();
        }
        catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
    }

    public static boolean checkVersion( Database database )
    {
        boolean versionOkay = true;
        Connection connection = database.getConnection();
        try {
            int version = 0;
            String statement = "SELECT value FROM player_notes_other WHERE key='VERSION'";
            PreparedStatement preparedStatement = connection.prepareStatement(statement.toString());
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

    public static void createNote( Database database, String notingPlayer, String aboutPlayer, String note )
    {
        Connection connection = database.getConnection();
        try {
            String statement = "INSERT INTO player_notes(player, note_by, note) VALUES (?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement( statement );
            preparedStatement.setString( 1, aboutPlayer );
            preparedStatement.setString( 2, notingPlayer );
            preparedStatement.setString( 3, note );
            preparedStatement.execute();
            preparedStatement.close();
        }
        catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
    }

    public static ArrayList<String> viewNotes( Database database, String aboutPlayerFilter )
    {
        Connection connection = database.getConnection();
        try {
            ArrayList<String> results = new ArrayList<>();
            StringBuilder statement = new StringBuilder();
            statement.append( "SELECT * FROM player_notes" );
            if(aboutPlayerFilter != null ) {
                statement.append( " WHERE player='" ).append( aboutPlayerFilter ).append( "'" );
            }
            statement.append( " ORDER BY id" );
            PreparedStatement preparedStatement = connection.prepareStatement( statement.toString() );
            ResultSet resultSet = preparedStatement.executeQuery();
            while( resultSet.next() ) {
                int id = resultSet.getInt("id");
                String notingPlayer = resultSet.getString("note_by");
                String player = resultSet.getString("player");
                String note = resultSet.getString("note");
                String result = ChatColor.GREEN + "#" + id + ": " + ChatColor.DARK_AQUA + player + ChatColor.WHITE + " - " + note + ChatColor.RED + " < " + notingPlayer;
                results.add(result);
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

    public static void deleteNotes( Database database, int id, String aboutPlayerFilter )
    {
        Connection connection = database.getConnection();
        try {
            StringBuilder statement = new StringBuilder();
            statement.append( "DELETE FROM player_notes" );
            if(aboutPlayerFilter != null ) {
                statement.append( " WHERE player='" ).append( aboutPlayerFilter ).append( "'" );
            } else if( id > 0 ) {
                statement.append( " WHERE id=" ).append( id );
            }
            PreparedStatement preparedStatement = connection.prepareStatement( statement.toString() );
            preparedStatement.execute();
            preparedStatement.close();
        }
        catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
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

