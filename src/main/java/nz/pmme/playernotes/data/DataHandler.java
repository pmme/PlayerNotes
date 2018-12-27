package nz.pmme.playernotes.data;


import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class DataHandler {
    public static void generateTables( Database database ) {
        Connection connection = database.getConnection();
        try {

            // Check a version number in a player_notes_settings table to check for database conversion requirements.


            PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS player_notes(id INTEGER PRIMARY KEY,player VARCHAR(255) NOT NULL,note_by VARCHAR(255) NOT NULL,note VARCHAR(255) NOT NULL)");
            preparedStatement.execute();
            preparedStatement.close();
        }
        catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
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

    public static void deleteNote( Database database, int id )
    {
        Connection connection = database.getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement( "DELETE FROM player_notes WHERE id=" + id );
            preparedStatement.execute();
            preparedStatement.close();
        }
        catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
    }

    public static void deleteAllNotes( Database database )
    {
        Connection connection = database.getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement( "DELETE FROM player_notes" );
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

