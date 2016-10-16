package infuzion.chest.randomizer.command;

import infuzion.chest.randomizer.ChestRandomizer;
import infuzion.chest.randomizer.util.Messages;
import infuzion.chest.randomizer.util.configItemStorageFormat;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class CommandAdmin {
    private final ChestRandomizer plugin;
    private final CommandSender sender;
    private final String[] args;

    CommandAdmin(ChestRandomizer plugin, CommandSender sender, Command cmd, String label, String[] args) {

        this.plugin = plugin;
        this.sender = sender;
        this.args = args;
        onCommand();
    }

    private void onCommand() {
        if (!sender.hasPermission("cr.admin")) {
            sender.sendMessage(Messages.error_permission);
        }
        if (args.length < 2) {
            sender.sendMessage(Messages.admin_help);
        }
        if (args[1].equalsIgnoreCase("add")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Messages.error_player);
                return;
            }

            Player p = (Player) sender;
            if ((args.length < 3)) {
                sender.sendMessage(Messages.admin_add_help);
                return;
            }

            try {
                int percent = Integer.parseInt(args[2]);
                ItemStack item = p.getInventory().getItemInMainHand();
                if (item == null || item.getType().equals(Material.AIR)) {
                    sender.sendMessage(Messages.admin_add_noitem);
                    return;
                }
                if (args.length > 3) {
                    if (!plugin.getConfigManager().addConfig(new configItemStorageFormat(item, percent), args[3])) {
                        p.sendMessage(Messages.error_group);
                        return;
                    }
                    sender.sendMessage(Messages.admin_add_success);
                    return;

                }
                if (!plugin.getConfigManager().addConfig(new configItemStorageFormat(item, percent))) {
                    p.sendMessage(Messages.error_unknown);
                    return;
                }
                sender.sendMessage(Messages.admin_add_success);
            } catch (Exception e) {
                p.sendMessage(Messages.error_unknown);
            }
        } else if (args[1].equalsIgnoreCase("remove")) {
            if ((args.length < 3)) {
                sender.sendMessage(Messages.admin_remove_help);
                return;
            }

            if (plugin.getConfigManager().groupExists(args[2])) {
                HashMap<CommandSender, Integer> confirmations = plugin.getConfirmations();
                confirmations.put(sender, 30);
                plugin.setConfirmations(confirmations);
                plugin.addToConfirmationGroups(sender, args[2]);
                sender.sendMessage(Messages.admin_remove_prompt);
            } else {
                sender.sendMessage(Messages.error_group);
            }
        } else if (args[1].equalsIgnoreCase("create")) {
            if (args.length < 3) {
                sender.sendMessage(Messages.admin_create_help);
                return;
            }

            if (plugin.getConfigManager().groupExists(args[2])) {
                sender.sendMessage(Messages.admin_create_exists);
                return;
            }

            String group = args[2];
            List<String> toSet = new ArrayList<String>();
//            toSet.add("placeholder");
            plugin.getConfigManager().set("Groups." + group, toSet);
            sender.sendMessage(Messages.admin_create_success);
        }
    }
}
