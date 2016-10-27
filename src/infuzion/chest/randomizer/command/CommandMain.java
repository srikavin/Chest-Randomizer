package infuzion.chest.randomizer.command;

import infuzion.chest.randomizer.ChestRandomizer;
import infuzion.chest.randomizer.storage.chestLocation;
import infuzion.chest.randomizer.storage.chestManager;
import infuzion.chest.randomizer.util.Direction;
import infuzion.chest.randomizer.util.configuration.configItemStorageFormat;
import infuzion.chest.randomizer.util.configuration.configManager;
import infuzion.chest.randomizer.util.messages.Messages;
import io.netty.util.internal.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommandMain implements CommandExecutor {
    private static final int[] axis = {3, 4, 2, 5};
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();
    private static int min;
    private static int max;
    private final ChestRandomizer pl;
    private final configManager configManager;
    private final chestManager chestManager;


    public CommandMain(ChestRandomizer pl) {
        this.pl = pl;
        this.configManager = pl.getConfigManager();
        this.chestManager = pl.getChestManager();
        max = pl.getConfig().getInt("ChestRandomizer.RandomizerSettings.MaximumItems");
        min = pl.getConfig().getInt("ChestRandomizer.RandomizerSettings.MinimumItems");
        if (max < 0) {
            max = 0;
        }
        if (min < 0) {
            min = 0;
        }
    }

    private static int yawToFace(float yaw) {
        return axis[Math.round(yaw / 90f) & 0x3];
    }

    public static boolean randomizeChest(Location location, int facing, final String group, final ChestRandomizer pl) {

        //Position and face the chest properly
        location.getBlock().setType(Material.CHEST);
        final Chest chest = (Chest) location.getBlock().getState();
        final Inventory chestInv = chest.getBlockInventory();

        //noinspection deprecation
        chest.setRawData((byte) facing);
        //Randomize items inside the chest
        chestInv.setContents(new ItemStack[]{});

        new BukkitRunnable() {
            public void run() {
                final List<configItemStorageFormat> toAdd = pl.getConfigManager().getConfigValue(group);
                if (toAdd.size() <= 0) {
                    pl.getLogger().warning(pl.getPrefix() + " Group " + group + " is empty. This will result in an empty    chest");
                    return;
                }
                int ritems = random.nextInt(max + 1);
                if (ritems < min) {
                    ritems = min;
                }

                List<ItemStack> items = new ArrayList<ItemStack>();

                final int toAddSize = toAdd.size();
                for (int i = 0; i < ritems; i += 0) {
                    int slot = random.nextInt(toAddSize);
                    configItemStorageFormat cur = toAdd.get(slot);
                    if (pl.randomize(cur.getPercent())) {
                        if (cur.getItem() != null) {
                            items.add(cur.getItem());
                            i++;
                        }
                    }
                }


                for (ItemStack e : items) {
                    int slot = random.nextInt(27);
                    if (chestInv.getItem(slot) == null) {
                        chestInv.setItem(slot, e);
                    } else if (e != null) {
                        chestInv.addItem(e);
                    }
                }
            }
        }.runTaskAsynchronously(pl);
        return true;
    }

    private void adminCommand(ChestRandomizer plugin, CommandSender sender, String[] args) {
        if (!sender.hasPermission("cr.admin")) {
            sender.sendMessage(Messages.error_permission);
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Messages.admin_help);
            return;
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
            plugin.getConfigManager().set("Groups." + group, toSet);
            sender.sendMessage(Messages.admin_create_success);
        } else {
            sender.sendMessage(Messages.admin_help);
        }
    }

    private int getDirection(String dir) {
        char direction = dir.toLowerCase().trim().charAt(0);
        int facing;

        switch (direction) {
            case 'n':
                facing = 2;
                break;
            case 's':
                facing = 3;
                break;
            case 'e':
                facing = 5;
                break;
            case 'w':
                facing = 4;
                break;
            default:
                facing = -1;
                break;
        }
        return facing;
    }

    private String getHelp(CommandSender sender) {
        StringBuilder help = new StringBuilder();
        if (sender.hasPermission("cr.reload")) {
            help.append(Messages.help_reload).append("\n");
        }
        if (sender.hasPermission("cr.randomize")) {
            help.append(Messages.help_randomize).append("\n");
        }
        if (sender.hasPermission("cr.admin")) {
            help.append(Messages.help_admin).append("\n");
        }
        if (sender.hasPermission("cr.randomizeall")) {
            help.append(Messages.help_randomizeall).append("\n");
        }
        String toSend = help.toString();
        if (toSend.equals("")) {
            toSend = Messages.help_empty;
        }
        return toSend;
    }

    /**
     * {@inheritDoc}
     */
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(getHelp(sender));
            return true;
        } else if (args[0].equalsIgnoreCase("help") || args[0].trim().equalsIgnoreCase("")) {
            sender.sendMessage(getHelp(sender));
            return true;
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("cr.reload")) {
                sender.sendMessage(Messages.error_permission);
                return true;
            }
            pl.reloadConfig();
            sender.sendMessage(Messages.reload_success);
            return true;
        } else if (args[0].equalsIgnoreCase("randomizeall")) {
            randomizeAll(pl, sender, args);
            return true;
        } else if (args[0].equalsIgnoreCase("admin")) {
            adminCommand(pl, sender, args);
            return true;
        } else if (args[0].equalsIgnoreCase("updater")) {
            if (!sender.hasPermission("cr.opt")) {
                sender.sendMessage(Messages.error_permission);
                return true;
            }
            if (args[1].equalsIgnoreCase("opt-in")) {
                sender.sendMessage(Messages.updater_optin);
                configManager.set("Updater.Opt-Out", false);
            } else if (args[1].equalsIgnoreCase("opt-out")) {
                sender.sendMessage(Messages.updater_optout);
                configManager.set("Updater.Opt-Out", true);
            }
            return true;
        } else if (args[0].equalsIgnoreCase("randomize") || args[0].equalsIgnoreCase("r")) {
            String permissionGroup = "default";
            if (args.length < 3 && args.length > 2) {
                permissionGroup = args[1];
            }
            if (!sender.hasPermission("cr.randomize." + permissionGroup)) {
                sender.sendMessage(Messages.error_permission);
                return true;
            }
            if (sender instanceof Player && (args.length < 3 || !sender.hasPermission("cr.location." + args[1]))) {
                Player p = ((Player) sender);
                Location location = p.getLocation();

                String group = "default";
                if (args.length > 1) {
                    group = args[1];
                }

                if (configManager.groupExists(group)) {
                    randomizeChest(location, yawToFace(location.getYaw()), group, pl);
                    pl.getChestManager().addChest(location, Direction.valueOf(yawToFace(location.getYaw())), group);
                    sender.sendMessage(Messages.randomize_success);
                } else {
                    sender.sendMessage(Messages.error_group);
                }
                return true;
            } else if (args.length > 4) { // /cr r [group] <x> <y> <z> <facing> [world]
                try {
                    String group = args[1];
                    String sX = args[2];
                    String sY = args[3];
                    String sZ = args[4];
                    String dir = "n";

                    if (group == null) {
                        group = "default";
                    }

                    if (sender instanceof Entity) {
                        Entity entity = (Entity) sender;
                        Location entityLocation = entity.getLocation();
                        sX = sX.replace("~", entityLocation.getX() + "");
                        sY = sY.replace("~", entityLocation.getY() + "");
                        sZ = sZ.replace("~", entityLocation.getZ() + "");
                    }

                    if (sender instanceof ProxiedCommandSender) {
                        ProxiedCommandSender proxiedCommandSender = (ProxiedCommandSender) sender;
                        Location entityLocation = ((Entity) proxiedCommandSender.getCallee()).getLocation();
                        sX = sX.replace("~", entityLocation.getX() + "");
                        sY = sY.replace("~", entityLocation.getY() + "");
                        sZ = sZ.replace("~", entityLocation.getZ() + "");
                    }
                    Double x = Double.parseDouble(sX);
                    Double y = Double.parseDouble(sY);
                    Double z = Double.parseDouble(sZ);
                    Location loc = new Location(Bukkit.getWorlds().get(0), x, y, z);

                    if (args.length > 5 && args[5] != null) {
                        dir = args[5];
                    }

                    if (args.length > 6) {
                        if (Bukkit.getWorld(args[6]) != null) {
                            loc.setWorld(Bukkit.getWorld(args[6]));
                        } else {
                            sender.sendMessage(Messages.error_world);
                            return true;
                        }
                    }

                    boolean result;
                    if (configManager.groupExists(group)) {
                        result = randomizeChest(loc, dir, group);
                    } else {
                        sender.sendMessage(Messages.error_group);
                        return true;
                    }
                    if (!result) {
                        sender.sendMessage(Messages.error_direction);
                        return true;
                    }
                    sender.sendMessage(Messages.randomize_success);
                    pl.getChestManager().addChest(loc, Direction.valueOf(getDirection(dir)), group);

                } catch (NumberFormatException e) {
                    sender.sendMessage(Messages.error_number);
                }
            }
            return true;
        }
        sender.sendMessage(getHelp(sender));
        return true;
    }

    /**
     * Randomizes all of the chests that meet the specifications
     *
     * @param plugin ChestRandomizer instance
     * @param sender Sender that sent the command
     * @param args   Arguments sent with the command
     */
    private void randomizeAll(ChestRandomizer plugin, CommandSender sender, String[] args) {
        if (args.length < 2 || args[1].length() == 0) {
            for (String e : Messages.randomizeall_help.split("\n")) {
                sender.sendMessage(e);
            }
            return;
        }
        if (args[1].equalsIgnoreCase("all")) {
            randomizeAll(chestManager.getAllChests(), sender);
        } else if (plugin.getConfigManager().groupExists(args[1])) {
            randomizeAll(chestManager.getAllChestsInGroup(args[1]), sender);
        } else if (args.length == 7 || args.length == 8) { //cr randomizeall [x1] [y1] [z1] [x1] [y2] [z2]
            try {
                double x1 = Double.parseDouble(args[1]);
                double y1 = Double.parseDouble(args[2]);
                double z1 = Double.parseDouble(args[3]);
                double x2 = Double.parseDouble(args[4]);
                double y2 = Double.parseDouble(args[5]);
                double z2 = Double.parseDouble(args[6]);
                Location loc1 = new Location(Bukkit.getWorlds().get(0), x1, y1, z1);
                Location loc2 = new Location(Bukkit.getWorlds().get(0), x2, y2, z2);

                if (args.length == 8) {
                    if (Bukkit.getWorld(args[7]) != null) {
                        World world = Bukkit.getWorld(args[8]);
                        loc1.setWorld(world);
                        loc2.setWorld(world);
                    } else {
                        sender.sendMessage(Messages.error_world);
                        return;
                    }
                }
                randomizeAll(chestManager.getAllChestsInCuboid(loc1, loc2), sender);
            } catch (NumberFormatException e) {
                sender.sendMessage(Messages.error_number);
            }
        } else if (args.length == 5 || args.length == 6) { //cr randomizeall [x] [y] [z] [radius] [world]
            try {
                if (sender instanceof Entity) {
                    Entity entity = (Entity) sender;
                    if (args[1].equalsIgnoreCase("~")) {
                        args[1] = String.valueOf(entity.getLocation().getX());
                    }
                    if (args[2].equalsIgnoreCase("~")) {
                        args[2] = String.valueOf(entity.getLocation().getY());
                    }
                    if (args[3].equalsIgnoreCase("~")) {
                        args[3] = String.valueOf(entity.getLocation().getZ());
                    }
                }
                double x = Double.parseDouble(args[1]);
                double y = Double.parseDouble(args[2]);
                double z = Double.parseDouble(args[3]);
                int radius = Integer.parseInt(args[4]);
                Location loc = new Location(Bukkit.getWorlds().get(0), x, y, z);

                if (args.length == 6) {
                    if (Bukkit.getWorld(args[5]) != null) {
                        World world = Bukkit.getWorld(args[8]);
                        loc.setWorld(world);
                    } else {
                        sender.sendMessage(Messages.error_world);
                        return;
                    }
                }
                randomizeAll(chestManager.getAllChestsInSpheroid(loc, radius), sender);
            } catch (NumberFormatException e) {
                sender.sendMessage(Messages.error_number);
            }
        } else {
            for (String e : Messages.randomizeall_help.split("\n")) {
                sender.sendMessage(e);
            }
        }
    }

    private int randomizeAll(final List<chestLocation> list, final CommandSender sender, final boolean recursive) {
        final int[] counter = {0};
        if (list.size() < 101) {
            for (chestLocation e : list) {
                chestManager.randomize(e);
                counter[0]++;
            }
        } else {
            final int listSize = list.size();
            new BukkitRunnable() {
                public void run() {
                    counter[0] += randomizeAll(list.subList(0, listSize / 2), sender, true);
                    counter[0] += randomizeAll(list.subList(listSize / 2, listSize), sender, true);
                    if (!recursive) {
                        sender.sendMessage(Messages.randomizeall_success.replace("%amount%", String.valueOf(listSize)));
                    }
                }
            }.runTaskLater(pl, 4);
        }
        return counter[0];
    }

    private void randomizeAll(List<chestLocation> locations, CommandSender sender) {
        randomizeAll(locations, sender, false);
    }

    private boolean randomizeChest(Location loc, String dir, String group) {
        int facing = getDirection(dir);
        if (facing > 1) {
            randomizeChest(loc, facing, group, pl);
            return true;
        } else {
            return randomizeChest(loc, "n", group);
        }
    }
}
