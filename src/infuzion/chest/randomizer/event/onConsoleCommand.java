package infuzion.chest.randomizer.event;

import infuzion.chest.randomizer.ChestRandomizer;
import infuzion.chest.randomizer.util.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.HashMap;

public class onConsoleCommand implements Listener {

    private ChestRandomizer plugin;

    public onConsoleCommand(ChestRandomizer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onConsoleCommandEvent(ServerCommandEvent event) {
        if (event.getCommand().equalsIgnoreCase("confirm")) {
            CommandSender sender = event.getSender();
            if (plugin.getConfirmations().containsKey(sender)) {
                HashMap<CommandSender, Integer> confirmations = plugin.getConfirmations();
                confirmations.remove(event.getSender());
                plugin.setConfirmations(confirmations);
                String group = plugin.getConfirmationGroups(sender);
                plugin.getConfigManager().removeGroup(group);
                sender.sendMessage(Messages.admin_remove_success.replace("<group>", group));
                event.setCancelled(true);
            }
        }
    }
}
