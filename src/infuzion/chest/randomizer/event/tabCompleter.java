package infuzion.chest.randomizer.event;

import infuzion.chest.randomizer.ChestRandomizer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class tabCompleter implements TabCompleter {
    private ChestRandomizer pl;

    public tabCompleter(ChestRandomizer pl) {
        this.pl = pl;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> ls = new ArrayList<String>();
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length == 1) {
                //cr r [group]
                List<String> possible = new ArrayList<String>();
                if (sender.hasPermission("cr.access")) {
                    possible.add("randomize");
                }
                if (sender.hasPermission("cr.admin")) {
                    possible.add("admin");
                }
                if (sender.hasPermission("cr.reload")) {
                    possible.add("reload");
                }
                if (sender.hasPermission("cr.opt")) {
                    possible.add("updater");
                    possible.add("metrics");
                }
                return pl.possibilityChecker(possible, args[0]);
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("r") || args[0].equalsIgnoreCase("randomize")) {
                    List<String> possible = new ArrayList<String>();
                    for (String e : pl.getConfigManager().getGroupNames()) {
                        if (sender.hasPermission("cr.randomize." + e)) {
                            possible.add(e);
                        }
                    }
                    return pl.possibilityChecker(possible, args[1]);
                } else if (args[0].equalsIgnoreCase("admin")) {
                    List<String> possible = new ArrayList<String>();
                    possible.add("add");
                    possible.add("remove");
                    return pl.possibilityChecker(possible, args[1]);
                }
            }
        }
        return ls;
    }


}
