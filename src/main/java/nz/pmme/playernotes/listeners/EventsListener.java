package nz.pmme.playernotes.listeners;

import nz.pmme.playernotes.PlayerNotes;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventsListener implements Listener
{
    private PlayerNotes plugin;
    public EventsListener(PlayerNotes plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        if( event.getPlayer().hasPermission("playernotes.view") ) {
            if( plugin.getDataHandler().hasUnreadNotes( event.getPlayer().getUniqueId() ) ) {
                event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "[PlayerNotes] " + ChatColor.LIGHT_PURPLE + "There are new notes since your last check.");
            }
        }
    }
}
