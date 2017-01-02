package infuzion.chest.randomizer.event;

import infuzion.chest.randomizer.ChestRandomizer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class tabCompleter implements TabCompleter {
    private ChestRandomizer pl;

    public tabCompleter(ChestRandomizer pl) {
        this.pl = pl;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> possible = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length == 1) {
                //cr r [group]
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
                if (sender.hasPermission("cr.randomizeall")) {
                    possible.add("randomizeall");
                }
                return pl.possibilityChecker(possible, args[0]);
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("r") || args[0].equalsIgnoreCase("randomize")) {
                    for (String e : pl.getConfigManager().getGroupNames()) {
                        if (sender.hasPermission("cr.randomize." + e)) {
                            possible.add(e);
                        }
                    }
                    return pl.possibilityChecker(possible, args[1]);
                } else if (args[0].equalsIgnoreCase("randomizeall")) {
                    for (String e : pl.getConfigManager().getGroupNames()) {
                        if (sender.hasPermission("cr.randomize." + e)) {
                            possible.add(e);
                        }
                    }
                    return pl.possibilityChecker(possible, args[1]);
                } else if (args[0].equalsIgnoreCase("admin")) {
                    possible.add("add");
                    possible.add("remove");
                    possible.add("create");
                    return pl.possibilityChecker(possible, args[1]);
                }
            } else if (args.length == 3) {
                if (args[1].equalsIgnoreCase("remove")) {
                    for (String e : pl.getConfigManager().getGroupNames()) {
                        if (sender.hasPermission("cr.randomize." + e)) {
                            possible.add(e);
                        }
                    }
                    return pl.possibilityChecker(possible, args[2]);
                }
            } else if (args.length == 4) {
                if (args[1].equalsIgnoreCase("add")) {
                    for (String e : pl.getConfigManager().getGroupNames()) {
                        if (sender.hasPermission("cr.randomize." + e)) {
                            possible.add(e);
                        }
                    }
                    return pl.possibilityChecker(possible, args[3]);
                }
            }
        }
        return new ArrayList<>();
    }


}
