package infuzion.chest.randomizer.command;

import infuzion.chest.randomizer.ChestRandomizer;
import infuzion.chest.randomizer.util.configuration.configItemStorageFormat;
import infuzion.chest.randomizer.util.configuration.configManager;
import infuzion.chest.randomizer.util.messages.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CommandMain implements CommandExecutor {
    private static final int[] axis = {3, 4, 2, 5};
    private final ChestRandomizer pl;
    private final configManager configManager;
    private final Random random;

    public CommandMain(ChestRandomizer pl) {
        this.pl = pl;
        this.configManager = pl.getConfigManager();
        random = new Random();
    }

    private static int yawToFace(float yaw) {
        return axis[Math.round(yaw / 90f) & 0x3];
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
                pl.onDisable();
                pl.onEnable();
                sender.sendMessage(Messages.reload_success);
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
                        randomizeChest(location, yawToFace(location.getYaw()), group);
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

    private boolean randomizeChest(Location location, int facing) {
        return randomizeChest(location, facing, "default");
    }

    private boolean randomizeChest(Location location, int facing, String group) {
        //Position and face the chest properly
        location.getBlock().setType(Material.CHEST);
        BlockState chest = location.getBlock().getState();
        chest.setRawData((byte) facing);

        //Randomize the amount of items inside the chest
        int max = pl.getConfig().getInt("ChestRandomizer.RandomizerSettings.MaximumItems");
        int min = pl.getConfig().getInt("ChestRandomizer.RandomizerSettings.MinimumItems");
        int ritems = random.nextInt(max + 1);
        if (ritems < min) {
            ritems = min;
        }

        //Randomize items inside the chest
        Inventory chestInv = ((Chest) location.getBlock().getState()).getBlockInventory();
        List<configItemStorageFormat> toAdd = configManager.getConfigValue(group);
        if (toAdd.size() == 0) {
            pl.getLogger().warning(pl.getPrefix() + " Group " + group + " is empty. This will result in an empty chest  ");
            return true;
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
            }
        }

        chest.update();
        return true;
    }

    private boolean randomizeChest(Location loc, String dir, String group) {
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

        if (facing > 1) {
            randomizeChest(loc, facing, group);
            return true;
        } else {
            return randomizeChest(loc, "n", group);
        }
    }
}
