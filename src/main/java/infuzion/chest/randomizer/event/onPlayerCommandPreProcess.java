package infuzion.chest.randomizer.event;

import infuzion.chest.randomizer.ChestRandomizer;
import infuzion.chest.randomizer.util.messages.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class onPlayerCommandPreProcess implements Listener {
    private static final Pattern GROUP_VARIABLE = Pattern.compile("<group>", Pattern.LITERAL);
    private ChestRandomizer plugin;

    public onPlayerCommandPreProcess(ChestRandomizer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void playerCommandPreProcessEvent(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().equalsIgnoreCase("/confirm")) {
            Player player = event.getPlayer();
            if (plugin.getConfirmations().containsKey(player)) {
                event.setCancelled(true);

                Map<CommandSender, Integer> confirmations = plugin.getConfirmations();
                confirmations.remove(player);
                plugin.setConfirmations(confirmations);
                String group = plugin.getConfirmationGroups(player);

                plugin.getConfigManager().removeGroup(group);
                player.sendMessage(GROUP_VARIABLE.matcher(Messages.admin_remove_success)
                        .replaceAll(Matcher.quoteReplacement(group)));
            }
        }
    }
}
