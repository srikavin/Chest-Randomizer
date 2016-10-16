package infuzion.chest.randomizer.event;

import infuzion.chest.randomizer.ChestRandomizer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;

public class onPlayerDisconnect implements Listener {
    private ChestRandomizer plugin;

    public onPlayerDisconnect(ChestRandomizer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void playerDisconnect(PlayerQuitEvent e) {
        if (plugin.getConfirmations().containsKey(e.getPlayer())) {
            HashMap<CommandSender, Integer> confirmations = plugin.getConfirmations();
            confirmations.remove(e.getPlayer());
            plugin.setConfirmations(confirmations);
        }
    }

}
