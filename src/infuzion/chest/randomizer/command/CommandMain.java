package infuzion.chest.randomizer.command;

import infuzion.chest.randomizer.ChestRandomizer;
import infuzion.chest.randomizer.storage.chestLocation;
import infuzion.chest.randomizer.storage.chestManager;
import infuzion.chest.randomizer.util.Direction;
import infuzion.chest.randomizer.util.configuration.configItemStorageFormat;
import infuzion.chest.randomizer.util.configuration.configManager;
import infuzion.chest.randomizer.util.messages.Messages;
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
import java.util.List;
import java.util.Random;

public class CommandMain implements CommandExecutor {
    private static final int[] axis = {3, 4, 2, 5};
    private static final Random random = new Random();
    private static int min;
    private static int max;
    private final ChestRandomizer pl;
    private final configManager configManager;


    public CommandMain(ChestRandomizer pl) {
        this.pl = pl;
        this.configManager = pl.getConfigManager();
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
        //noinspection deprecation
        chest.setRawData((byte) facing);

        //Randomize items inside the chest
        final Inventory chestInv = chest.getBlockInventory();

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

                for (int i = 0; i < ritems; i++) {
                    if (pl.randomize(toAdd.get((i % toAdd.size())).getPercent())) {
                        items.add(toAdd.get((i % toAdd.size())).getItem());
                    }
                }


                for (ItemStack e : items) {
                    int slot = random.nextInt(27);
                    if (chestInv.getItem(slot) == null) {
                        chestInv.setItem(slot, e);
                    } else {
                        chestInv.addItem(e);
                    }
                }
            }
        }.runTaskAsynchronously(pl);
        return true;
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

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("help") || args[0].trim().equalsIgnoreCase("")) {
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
                new CommandAdmin(pl, sender, cmd, label, args);
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
                } else if (args[1].equalsIgnoreCase("update-now")) {
                    pl.startUpdater();
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

                        pl.getLogger().severe(String.valueOf(sender.getClass()));

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
        }
        sender.sendMessage(getHelp(sender));
        return true;
    }

    private void randomizeAll(ChestRandomizer plugin, CommandSender sender, String[] args) {
        int counter = 0;
        chestManager chestManager = plugin.getChestManager();


        if (args.length < 2 || args[1].length() == 0) {
            for (String e : Messages.randomizeall_help.split("\n")) {
                sender.sendMessage(e);
            }
            return;
        }
        if (args[1].equalsIgnoreCase("all")) {
            for (chestLocation e : chestManager.getAllChests()) {
                chestManager.randomize(e);
                counter++;
            }
            sender.sendMessage(Messages.randomizeall_success.replace("%amount%", String.valueOf(counter)));
        } else if (plugin.getConfigManager().groupExists(args[1])) {
            for (chestLocation e : chestManager.getAllChestsInGroup(args[1])) {
                chestManager.randomize(e);
                counter++;
            }
            sender.sendMessage(Messages.randomizeall_success.replace("%amount%", String.valueOf(counter)));
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

                for (chestLocation e : chestManager.getAllChestsInCuboid(loc1, loc2)) {
                    chestManager.randomize(e);
                    counter++;
                }
                sender.sendMessage(Messages.randomizeall_success.replace("%amount%", String.valueOf(counter)));
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

                for (chestLocation e : chestManager.getAllChestsInSpheroid(loc, radius)) {
                    chestManager.randomize(e);
                    counter++;
                }
                sender.sendMessage(Messages.randomizeall_success.replace("%amount%", String.valueOf(counter)));
            } catch (NumberFormatException e) {
                sender.sendMessage(Messages.error_number);
            }
        } else {
            for (String e : Messages.randomizeall_help.split("\n")) {
                sender.sendMessage(e);
            }
        }
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
