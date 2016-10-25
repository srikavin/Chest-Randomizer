package infuzion.chest.randomizer.event;

import infuzion.chest.randomizer.ChestRandomizer;
import infuzion.chest.randomizer.util.messages.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;

public class onPlayerCommandPreprocess implements Listener {

    private ChestRandomizer plugin;

    public onPlayerCommandPreprocess(ChestRandomizer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void playerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().equalsIgnoreCase("/confirm")) {
            if (plugin.getConfirmations().containsKey(event.getPlayer())) {
                HashMap<CommandSender, Integer> confirmations = plugin.getConfirmations();
                confirmations.remove(event.getPlayer());
                plugin.setConfirmations(confirmations);
                String group = plugin.getConfirmationGroups(event.getPlayer());
                plugin.getConfigManager().removeGroup(group);
                event.getPlayer().sendMessage(Messages.admin_remove_success.replace("<group>", group));
                event.setCancelled(true);
            }
        }
    }
}
