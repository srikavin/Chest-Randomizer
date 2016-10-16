package infuzion.chest.randomizer;

import infuzion.chest.randomizer.command.CommandMain;
import infuzion.chest.randomizer.event.onConsoleCommand;
import infuzion.chest.randomizer.event.onPlayerCommandPreprocessEvent;
import infuzion.chest.randomizer.event.onPlayerDisconnect;
import infuzion.chest.randomizer.event.tabCompleter;
import infuzion.chest.randomizer.util.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.*;

public class ChestRandomizer extends JavaPlugin {

    @SuppressWarnings("FieldCanBeLocal")
    private final double version = 3.0d;
    private final HashMap<CommandSender, String> confirmationGroups = new HashMap<CommandSender, String>();
    private messagesManager messagesManager;
    private configManager configManager;
    private Random random;
    private String prefix;
    private HashMap<CommandSender, Integer> confirmations = new HashMap<CommandSender, Integer>();

    public void addToConfirmationGroups(CommandSender commandSender, String string) {
        confirmationGroups.put(commandSender, string);
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

    @Override
    public void onLoad() {
        random = new Random();
    }

    @Override
    public void onEnable() {
        messagesManager = new messagesManager(this);
        new Messages(this);

        prefix = Messages.variable_prefix;
        configManager = new configManager(this);


        getCommand("chestrandomizer").setExecutor(new CommandMain(this));
        getCommand("chestrandomizer").setTabCompleter(new tabCompleter(this));
        Bukkit.getPluginManager().registerEvents(new onPlayerCommandPreprocessEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new onPlayerDisconnect(this), this);
        Bukkit.getPluginManager().registerEvents(new onConsoleCommand(this), this);

        if (!getConfig().getBoolean("ChestRandomizer.Metrics.Opt-Out")) {
            try {
                new Metrics(this);
                Bukkit.getServer().getServerId();
            } catch (IOException e) {
                getLogger().warning("Could not Log Metric Statistics! Verify internet connection is available and try again!");
            }
        }
        if (!(getConfig().getBoolean("ChestRandomizer.Updater.Opt-Out"))) {
            startUpdater();
        }
        verifyConfirmations();

    }

    public void startUpdater() {
        new Updater(this, 83511, this.getFile(), Updater.UpdateType.DEFAULT, true);
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

}
