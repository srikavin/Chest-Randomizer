package infuzion.chest.randomizer.command;

import infuzion.chest.randomizer.ChestRandomizer;
import infuzion.chest.randomizer.storage.ChestLocation;
import infuzion.chest.randomizer.storage.ChestManager;
import infuzion.chest.randomizer.util.RandomizationGroup;
import infuzion.chest.randomizer.util.Utilities;
import infuzion.chest.randomizer.util.configuration.ChestRandomizationItem;
import infuzion.chest.randomizer.util.configuration.ConfigManager;
import infuzion.chest.randomizer.util.messages.Messages;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandMain implements CommandExecutor {
    private static final BlockFace[] axis = {BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST};
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();
    private static int min;
    private static int max;
    private final ChestRandomizer pl;
    private final ConfigManager configManager;
    private final ChestManager chestManager;


    public CommandMain(ChestRandomizer pl) {
        this.pl = pl;
        this.configManager = pl.getConfigManager();
        this.chestManager = pl.getChestManager();
        reloadMinMax();
    }

    private static BlockFace yawToFace(float yaw) {
        return axis[Math.round(yaw / 90f) & 0x3];
    }

    public static void randomizeChest(Location location, BlockFace facing, final RandomizationGroup group, final ChestRandomizer pl) {

        //Position and face the chest properly
        Block block = location.getBlock();
        block.setType(Material.CHEST);
        final Chest chest = (Chest) block.getState();
        final Inventory chestInv = chest.getBlockInventory();

        //noinspection deprecation
        chest.setData(new org.bukkit.material.Chest(facing));
        chest.update();
        chestInv.setContents(new ItemStack[]{});

        new BukkitRunnable() {
            public void run() {
                final List<ChestRandomizationItem> toAdd = pl.getConfigManager().getConfigValue(group);
                if (toAdd.size() <= 0) {
                    pl.getLogger().warning(pl.getPrefix() + " Group " + group + " is empty. This will result in an empty    chest");
                    return;
                }
                int ritems = random.nextInt(max + 1);
                if (ritems < min) {
                    ritems = min;
                }

                final List<ItemStack> items = new ArrayList<>();

                final int toAddSize = toAdd.size();
                for (int i = 0; i < ritems; i += 0) {
                    int slot = random.nextInt(toAddSize);
                    ChestRandomizationItem cur = toAdd.get(slot);
                    if (pl.randomize(cur.getPercent())) {
                        if (cur.getItem() != null) {
                            items.add(cur.getItem());
                            i++;
                        }
                    }
                }

                new BukkitRunnable() {
                    public void run() {
                        for (ItemStack e : items) {
                            int slot = random.nextInt(27);
                            if (chestInv.getItem(slot) == null) {
                                chestInv.setItem(slot, e);
                            } else if (e != null) {
                                chestInv.addItem(e);
                            }
                        }
                    }
                }.runTaskLater(pl, 0);
            }
        }.runTaskAsynchronously(pl);
    }

    private void reloadMinMax() {
        max = pl.getConfig().getInt("ChestRandomizer.RandomizerSettings.MaximumItems");
        min = pl.getConfig().getInt("ChestRandomizer.RandomizerSettings.MinimumItems");
        if (max < 0) {
            max = 0;
        }
        if (min < 0) {
            min = 0;
        }
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
                    if (!plugin.getConfigManager().addConfig(new ChestRandomizationItem(item, percent), args[3])) {
                        p.sendMessage(Messages.error_group);
                        return;
                    }
                    sender.sendMessage(Messages.admin_add_success);
                    return;

                }
                if (!plugin.getConfigManager().addConfig(new ChestRandomizationItem(item, percent))) {
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
                Map<CommandSender, Integer> confirmations = plugin.getConfirmations();
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
            List<String> toSet = new ArrayList<>();
            plugin.getConfigManager().set("Groups." + group, toSet);
            sender.sendMessage(Messages.admin_create_success);
        } else {
            sender.sendMessage(Messages.admin_help);
        }
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
        } else if (args[0].length() < 1 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(getHelp(sender));
            return true;
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("cr.reload")) {
                sender.sendMessage(Messages.error_permission);
                return true;
            }
            pl.reloadConfig();
            reloadMinMax();
            sender.sendMessage(Messages.reload_success);
            return true;
        } else if (args[0].equalsIgnoreCase("randomizeall")) {
            randomizeAll(pl, sender, args);
            return true;
        } else if (args[0].equalsIgnoreCase("stress")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    final int[] i = {0};
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (i[0] > 255) {
                                cancel();
                            }
                            System.out.println(Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                    "execute @e ~ ~ ~ /cr r default ~ " + i[0] + " ~"));
                            i[0]++;
                        }
                    }.runTaskTimer(pl, 0, 2);
                }
            }.runTaskAsynchronously(pl);
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
                    randomizeChest(location, yawToFace(location.getYaw()), RandomizationGroup.getGroup(group), pl);
                    pl.getChestManager().addChest(location, yawToFace(location.getYaw()), group);
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

                    Location loc = Utilities.parseLocation(sX, sY, sZ, sender);

                    if (sender instanceof ProxiedCommandSender) {
                        loc = Utilities.parseLocation(sX, sY, sZ, ((ProxiedCommandSender) sender).getCallee());
                    }

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
                        result = randomizeChest(loc, dir, RandomizationGroup.getGroup(group));
                    } else {
                        sender.sendMessage(Messages.error_group);
                        return true;
                    }
                    if (!result) {
                        sender.sendMessage(Messages.error_direction);
                        return true;
                    }
                    sender.sendMessage(Messages.randomize_success);
                    pl.getChestManager().addChest(loc, Utilities.stringToBlockFace(dir), group);

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
            randomizeAll(chestManager.getAllChestsInGroup(RandomizationGroup.getGroup(args[1])), sender);
        } else if (args.length == 7 || args.length == 8) { //cr randomizeall [x1] [y1] [z1] [x1] [y2] [z2]
            Location loc1 = Utilities.parseLocation(args[1], args[2], args[3], sender);
            Location loc2 = Utilities.parseLocation(args[4], args[5], args[6], sender);
            if (loc1 == null) {
                return;
            }
            if (loc2 == null) {
                return;
            }

            if (args.length == 8) {
                World world = Bukkit.getWorld(args[8]);
                if (world != null) {
                    loc1.setWorld(world);
                    loc2.setWorld(world);
                } else {
                    sender.sendMessage(Messages.error_world);
                    return;
                }
            }
            randomizeAll(chestManager.getAllChestsInCuboid(loc1, loc2), sender);
        } else if (args.length == 5 || args.length == 6) { //cr randomizeall [x] [y] [z] [radius] [world]
            Location loc = Utilities.parseLocation(args[1], args[2], args[3], sender);
            if (loc == null) {
                return;
            }
            int radius = Integer.parseInt(args[4]);

            if (args.length == 6) {
                if (Bukkit.getWorld(args[5]) != null) {
                    loc.setWorld(Bukkit.getWorld(args[5]));
                } else {
                    sender.sendMessage(Messages.error_world);
                    return;
                }
            }
            randomizeAll(chestManager.getAllChestsInSpheroid(loc, radius), sender);
        } else {
            for (String e : Messages.randomizeall_help.split("\n")) {
                sender.sendMessage(e);
            }
        }
    }

    private void randomizeAll(final List<ChestLocation> list, final CommandSender sender, final boolean recursive,
                              final AtomicInteger amountDone, final int totalSize) {
        final int size = list.size();
        if (!recursive) {
            new BukkitRunnable() {
                private final NumberFormat percentageFormat = NumberFormat.getPercentInstance();
                private final String notDoneColor = ChatColor.RED.toString();
                private final String doneColor = ChatColor.GREEN.toString();
                private final char progressDoneChar = '\u2588';
                private final char progressNotDoneChar = '\u2588';
                private final int amount = 10;
                private boolean sent25percent = false;
                private boolean sent50percent = false;

                private String generateProgressBar(double percent) {
                    int sliceAmount = 100 / amount;
                    double slicesDone = percent * 100 / sliceAmount;
                    StringBuilder toRet = new StringBuilder();
                    int i;
                    toRet.append(doneColor);
                    for (i = 0; i <= slicesDone; i++) {
                        toRet.append(progressDoneChar);
                    }
                    toRet.append(notDoneColor);
                    for (; i <= amount; i++) {
                        toRet.append(progressNotDoneChar);
                    }

                    return toRet.toString();
                }

                @Override
                public void run() {
                    percentageFormat.setMaximumFractionDigits(1);
                    while (true) {
                        double percent = amountDone.get() / (double) size;
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            player.sendTitle(Messages.randomizeall_percent_title_main.replace("%percent%",
                                    percentageFormat.format(percent)),
                                    Messages.randomizeall_percent_title_subtitle.replace("%progressbar%", generateProgressBar(percent)),
                                    1, 2, 2);
                            if (amountDone.get() >= size) {
                                sender.sendMessage(Messages.randomizeall_success.replace("%amount%",
                                        String.valueOf(amountDone.get())));
                                break;
                            }
                        } else if (totalSize > 200) {
                            if (!sent25percent && amountDone.get() >= size / 4) {
                                sender.sendMessage(Messages.randomizeall_percent_chat.replace("%percent%",
                                        percentageFormat.format(percent)).replace("%progressbar%",
                                        generateProgressBar(percent)));
                                sent25percent = true;
                            }
                            if (!sent50percent && amountDone.get() >= size / 2) {
                                sender.sendMessage(Messages.randomizeall_percent_chat.replace("%percent%",
                                        percentageFormat.format(percent)).replace("%progressbar%",
                                        generateProgressBar(percent)));
                                sent50percent = true;
                            }
                        }
                        if (amountDone.get() >= size) {
                            sender.sendMessage(Messages.randomizeall_success.replace("%amount%",
                                    String.valueOf(amountDone.get())));
                            cancel();
                            return;
                        }
                        try {
                            Thread.sleep(25);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.runTaskAsynchronously(pl);
        }
        if (size <= 100 || size <= totalSize / 10) {
            for (ChestLocation e : list) {
                chestManager.randomize(e);
                amountDone.incrementAndGet();
            }
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    randomizeAll(list.subList(0, size / 10), sender, true, amountDone, totalSize);
                    randomizeAll(list.subList(size / 10, size), sender, true, amountDone, totalSize);
                }
            }.runTaskLater(pl, 1);
        }

    }

    private void randomizeAll(List<ChestLocation> locations, CommandSender sender) {
        randomizeAll(locations, sender, false, new AtomicInteger(0), locations.size());
    }

    private boolean randomizeChest(Location loc, String dir, RandomizationGroup group) {
        BlockFace facing = Utilities.stringToBlockFace(dir);
        if (facing != null) {
            randomizeChest(loc, facing, group, pl);
            return true;
        } else {
            return false;
        }
    }
}
