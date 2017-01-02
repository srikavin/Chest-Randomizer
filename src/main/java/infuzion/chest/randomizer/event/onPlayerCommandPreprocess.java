package infuzion.chest.randomizer.event;

import infuzion.chest.randomizer.ChestRandomizer;
import infuzion.chest.randomizer.util.messages.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Map;

public class onPlayerCommandPreprocess implements Listener {
    private ChestRandomizer plugin;

    public onPlayerCommandPreprocess(ChestRandomizer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void playerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().equalsIgnoreCase("/confirm")) {
            Player player = event.getPlayer();
            if (plugin.getConfirmations().containsKey(player)) {
                event.setCancelled(true);

                Map<CommandSender, Integer> confirmations = plugin.getConfirmations();
                confirmations.remove(player);
                plugin.setConfirmations(confirmations);
                String group = plugin.getConfirmationGroups(player);

                plugin.getConfigManager().removeGroup(group);
                player.sendMessage(Messages.admin_remove_success.replace("<group>", group));
            }
        }
    }
}
