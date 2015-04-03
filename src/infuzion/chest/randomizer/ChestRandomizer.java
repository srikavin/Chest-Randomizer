package infuzion.chest.randomizer;

import infuzion.chest.randomizer.command.CommandMain;
import infuzion.chest.randomizer.util.Metrics;
import infuzion.chest.randomizer.util.Updater;
import infuzion.chest.randomizer.util.configManager;
import infuzion.chest.randomizer.util.messagesManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Random;

public class ChestRandomizer extends JavaPlugin {

    private messagesManager messagesManager;
    private configManager configManager;
    private Random random;
    private String prefix;

    @Override
    public void onLoad() {
        random = new Random();
    }

    public void reloadPluginConfig() {
        messagesManager.reload();
        reloadConfig();
    }

    @Override
    public void onEnable() {
        messagesManager = new messagesManager(this);
        prefix = messagesManager.getMessage("Variables.Prefix");
        configManager = new configManager(this);
        getCommand("chestrandomizer").setExecutor(new CommandMain(this));
        if (!getConfig().getBoolean("ChestRandomizer.Metrics.Opt-Out")) {
            try {
                new Metrics(this);
            } catch (IOException e) {
                getLogger().warning("Could not Log Metric Statistics! Verify internet connection is available and try again!");
            }
        }
        if (!getConfig().getBoolean("ChestRandomizer.Updater.Opt-Out")) {
            startUpdater();
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public messagesManager getMessagesManager() {
        return messagesManager;
    }

    public configManager getConfigManager() {
        return configManager;
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

    public void startUpdater() {
        new Updater(this, 83511, this.getFile(), Updater.UpdateType.DEFAULT, true);
    }

}
