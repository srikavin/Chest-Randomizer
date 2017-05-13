package infuzion.chest.randomizer.util;

import infuzion.chest.randomizer.util.messages.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {
    private static final Pattern SELF_LOCATION = Pattern.compile("~", Pattern.LITERAL);
    private static boolean proxiedCommandSender;

    static {
        try {
            Class.forName("org.bukkit.command.ProxiedCommandSender");
            proxiedCommandSender = true;
        } catch (ClassNotFoundException e) {
            proxiedCommandSender = false;
        }
    }

    public static boolean proxiedCommandSenderExists() {
        return proxiedCommandSender;
    }

    public static Location parseLocation(String x, String y, String z, CommandSender sender) {
        return parseLocation(x, y, z, sender, Bukkit.getWorlds().get(0));
    }

    private static Location parseLocation(String x, String y, String z, CommandSender sender, World world) {
        System.out.println(proxiedCommandSender);
        if (sender instanceof Entity) {
            x = SELF_LOCATION.matcher(x).replaceAll(Matcher.quoteReplacement(String.valueOf(((Entity) sender).getLocation().getX())));
            y = SELF_LOCATION.matcher(y).replaceAll(Matcher.quoteReplacement(String.valueOf(((Entity) sender).getLocation().getY())));
            z = SELF_LOCATION.matcher(z).replaceAll(Matcher.quoteReplacement(String.valueOf(((Entity) sender).getLocation().getZ())));
        } else if (proxiedCommandSender && (sender instanceof ProxiedCommandSender) && (((ProxiedCommandSender) sender).getCallee() instanceof Entity)) {
            x = SELF_LOCATION.matcher(x).replaceAll(Matcher.quoteReplacement(String.valueOf(((Entity) ((ProxiedCommandSender) sender).getCallee()).getLocation().getX())));
            y = SELF_LOCATION.matcher(y).replaceAll(Matcher.quoteReplacement(String.valueOf(((Entity) ((ProxiedCommandSender) sender).getCallee()).getLocation().getY())));
            z = SELF_LOCATION.matcher(z).replaceAll(Matcher.quoteReplacement(String.valueOf(((Entity) ((ProxiedCommandSender) sender).getCallee()).getLocation().getZ())));
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

    public static BlockFace stringToBlockFace(String string) {
        String bFString;
        switch (string.toLowerCase().charAt(0)) {
            case 'n':
                bFString = "NORTH";
                break;
            case 's':
                bFString = "SOUTH";
                break;
            case 'e':
                bFString = "EAST";
                break;
            case 'w':
                bFString = "WEST";
                break;
            default:
                bFString = string;
                break;
        }
        return BlockFace.valueOf(bFString);
    }

    public static String center(String s, int size, char pad) {
        if ((s == null) || (size <= s.length())) {
            return s;
        }

        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < ((size - s.length()) / 2); i++) {
            sb.append(pad);
        }
        sb.append(s);
        while (sb.length() < size) {
            sb.append(pad);
        }
        return sb.toString();
    }

    public static List<String> possibilityChecker(List<String> possible, String toCompare) {
        List<String> toReturn = new ArrayList<>();
        if (toCompare.trim().equalsIgnoreCase("")) {
            toReturn.addAll(possible);
        } else {
            for (String e : possible) {
                if (e.startsWith(toCompare)) {
                    toReturn.add(e);
                }
            }
        }
        Collections.sort(toReturn);
        return toReturn;
    }
}
