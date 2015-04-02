package infuzion.chest.randomizer.command;

import infuzion.chest.randomizer.ChestRandomizer;
import infuzion.chest.randomizer.util.configManager;
import infuzion.chest.randomizer.util.configStorageFormat;
import infuzion.chest.randomizer.util.messagesManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.Random;

public class CommandMain implements CommandExecutor {
    private static final int[] axis = {3, 4, 2, 5};
    private final ChestRandomizer pl;
    private final messagesManager messagesManager;
    private final configManager configManager;
    private final Random random;

    public CommandMain (ChestRandomizer pl) {
        this.pl = pl;
        this.messagesManager = pl.getMessagesManager();
        this.configManager = pl.getConfigManager();
        random = new Random();
    }

    private static int yawToFace(float yaw) {
        return axis[Math.round(yaw / 90f) & 0x3];
    }

    public boolean onCommand (CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                pl.reloadPluginConfig();
                sender.sendMessage(messagesManager.getMessage("ReloadSuccess"));
            } else if (args[0].equalsIgnoreCase("updater")) {
                if (args[1].equalsIgnoreCase("opt-in")) {
                    configManager.set("Updater.Opt-Out", false);
                } else if (args[1].equalsIgnoreCase("opt-out")) {
                    configManager.set("Updater.Opt-Out", true);
                } else if (args[1].equalsIgnoreCase("update-now")) {
                    pl.startUpdater();
                }
            } else if (args[0].equalsIgnoreCase("randomize") || args[0].equalsIgnoreCase("r")) {
                if (sender instanceof Player && args.length == 1) {
                    Player p = ((Player) sender);
                    Location location = p.getLocation();

                    randomizeChest(location, yawToFace(location.getYaw()));
                } else if (args.length >= 4) { // /cr r <x> <y> <z> <facing> [world]
                    try {
                        Double x = Double.parseDouble(args[1]);
                        Double y = Double.parseDouble(args[2]);
                        Double z = Double.parseDouble(args[3]);
                        String dir;

                        Location loc = new Location(Bukkit.getWorlds().get(0), x, y, z);
                        if (args.length > 5) {
                            loc.setWorld(Bukkit.getWorld(args[5]));
                        }
                        if (args[4] == null) {
                            dir = "n";
                        } else {
                            dir = args[4];
                        }
                        if (!randomizeChest(loc, dir)) {
                            sender.sendMessage(messagesManager.getMessage("ChestRandomizationError.Direction"));
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage(pl.getPrefix() + messagesManager.getMessage("ChestRandomizationError.InvalidNumber"));
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void randomizeChest (Location location, int facing) {
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
        List<configStorageFormat> toAdd = configManager.getAllConfigValues();

        for (int i = 0; i < ritems; i++) {
            if (pl.randomize(toAdd.get((i % toAdd.size())).getPercent())) {
                chestInv.addItem(toAdd.get((i % toAdd.size())).getItem());
            }
        }

        chest.update();
    }

    private boolean randomizeChest (Location loc, String dir) {
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
            randomizeChest(loc, facing);
            return true;
        } else {
            return false;
        }
    }
}
