package infuzion.chest.randomizer.util;

import infuzion.chest.randomizer.util.messages.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.entity.Entity;

public class Utilities {
    public static Location parseLocation(String x, String y, String z, CommandSender sender) {
        return parseLocation(x, y, z, sender, Bukkit.getWorlds().get(0));
    }

    public static Location parseLocation(String x, String y, String z, CommandSender sender, World world) {
        if (sender instanceof Entity) {
            x = x.replace("~", ((Entity) sender).getLocation().getX() + "");
            y = y.replace("~", ((Entity) sender).getLocation().getY() + "");
            z = z.replace("~", ((Entity) sender).getLocation().getZ() + "");
        } else if (sender instanceof ProxiedCommandSender && ((ProxiedCommandSender) sender).getCallee() instanceof Entity) {
            x = x.replace("~", ((Entity) ((ProxiedCommandSender) sender).getCallee()).getLocation().getX() + "");
            y = y.replace("~", ((Entity) ((ProxiedCommandSender) sender).getCallee()).getLocation().getY() + "");
            z = z.replace("~", ((Entity) ((ProxiedCommandSender) sender).getCallee()).getLocation().getZ() + "");
        }
        try {
            Double retX = Double.parseDouble(x);
            Double retY = Double.parseDouble(y);
            Double retZ = Double.parseDouble(z);
            return new Location(world, retX, retY, retZ);
        } catch (Exception e) {
            sender.sendMessage(Messages.error_number);

            return null;
        }
    }
}
