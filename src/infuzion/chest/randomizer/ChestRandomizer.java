package infuzion.chest.randomizer;

import infuzion.chest.randomizer.command.CommandMain;
import infuzion.chest.randomizer.event.*;
import infuzion.chest.randomizer.storage.chestLocation;
import infuzion.chest.randomizer.storage.chestManager;
import infuzion.chest.randomizer.util.Metrics;
import infuzion.chest.randomizer.util.configuration.configItemStorageFormat;
import infuzion.chest.randomizer.util.configuration.configManager;
import infuzion.chest.randomizer.util.messages.Messages;
import infuzion.chest.randomizer.util.messages.messagesManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.update.spiget.SpigetUpdate;
import org.inventivetalent.update.spiget.UpdateCallback;

import java.io.IOException;
import java.util.*;

public class ChestRandomizer extends JavaPlugin {

    @SuppressWarnings("FieldCanBeLocal")
    private final double version = 3.5d;
    private final HashMap<CommandSender, String> confirmationGroups = new HashMap<CommandSender, String>();
    private messagesManager messagesManager;
    private configManager configManager;
    private chestManager chestManager;
    private Random random;
    private String prefix;
    private HashMap<CommandSender, Integer> confirmations = new HashMap<CommandSender, Integer>();

    public void addToConfirmationGroups(CommandSender commandSender, String string) {
        confirmationGroups.put(commandSender, string);
    }

    public chestManager getChestManager() {
        return chestManager;
    }

    public configManager getConfigManager() {
        return configManager;
    }

    public String getConfirmationGroups(CommandSender commandSender) {
        return confirmationGroups.get(commandSender);
    }

    public messagesManager getMessagesManager() {
        return messagesManager;
    }

    public String getPrefix() {
        return prefix;
    }

    public double getVersion() {
        return version;
    }

    public List<String> possibilityChecker(List<String> possible, String toCompare) {
        List<String> toReturn = new ArrayList<String>();
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

    public boolean randomize(int percent) {
        if (percent >= 100) {
            return true;
        } else if (percent < 0) {
            getLogger().severe("Negative Value detected in configuration file. This value will be ignored.");
        } else if (random.nextInt(101) <= percent) {
            return true;
        }
        return false;
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        if (configManager != null) {
            configManager.reload();
        }
        if (messagesManager != null) {
            messagesManager.reload();
        }
    }

    @Override
    public void onLoad() {
        random = new Random();
    }

    @Override
    public void onDisable() {
        chestManager.cleanUp();
    }

    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(configItemStorageFormat.class, "ChestRandomizationItem");
        ConfigurationSerialization.registerClass(chestLocation.class, "CRChestLocation");

        messagesManager = new messagesManager(this);
        new Messages(this);

        prefix = Messages.variable_prefix;

        configManager = new configManager(this);
        chestManager = new chestManager(this);


        getCommand("chestrandomizer").setExecutor(new CommandMain(this));
        getCommand("chestrandomizer").setTabCompleter(new tabCompleter(this));

        Bukkit.getPluginManager().registerEvents(new onPlayerCommandPreprocess(this), this);
        Bukkit.getPluginManager().registerEvents(new onPlayerDisconnect(this), this);
        Bukkit.getPluginManager().registerEvents(new onConsoleCommand(this), this);
        Bukkit.getPluginManager().registerEvents(new onBlockBreak(this), this);


        if (!getConfig().getBoolean("ChestRandomizer.Metrics.Opt-Out")) {
            try {
                Metrics metrics = new Metrics(this);
                metrics.start();
            } catch (IOException e) {
                getLogger().warning("Could not Log Metric Statistics! Verify internet connection is available and try again!");
            }
        }
        if (!(getConfig().getBoolean("ChestRandomizer.Updater.Opt-Out"))) {
            final SpigetUpdate updater = new SpigetUpdate(this, 30534);
            updater.checkForUpdate(new UpdateCallback() {
                public void updateAvailable(String newVersion, String downloadUrl, boolean hasDirectDownload) {
                    if (hasDirectDownload) {
                        updater.downloadUpdate();
                    }
                }

                public void upToDate() {
                }
            });
        }
        verifyConfirmations();

    }

    private void verifyConfirmations() {
        new BukkitRunnable() {
            public void run() {
                if (getConfirmations().isEmpty()) {
                    return;
                }

                for (CommandSender e : getConfirmations().keySet()) {
                    confirmations.put(e, confirmations.get(e) - 1);
                    if (confirmations.get(e) <= 0 || confirmations.get(e) > 31) {
                        String group = confirmationGroups.get(e);
                        e.sendMessage(Messages.admin_remove_timeout.replace("<group>", group));
                        confirmations.remove(e);
                        confirmationGroups.remove(e);
                    }
                }
            }
        }.runTaskTimer(this, 0, 20);
    }

    public HashMap<CommandSender, Integer> getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(HashMap<CommandSender, Integer> confirmations) {
        this.confirmations = confirmations;
    }

}