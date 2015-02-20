package infuzion.chest.randomizer;

import infuzion.chest.randomizer.command.CommandMain;
import infuzion.chest.randomizer.util.configManager;
import infuzion.chest.randomizer.util.messagesManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestRandomizer extends JavaPlugin {

    private messagesManager messagesManager;
    private configManager configManager;

    @Override
    public void onEnable() {
        messagesManager = new messagesManager(this);
        configManager = new configManager(this);
        getCommand("chestrandomizer").setExecutor(new CommandMain(this, messagesManager));
    }

    public messagesManager getMessagesManager(){
        return messagesManager;
    }

    public configManager getConfigManager(){
        return configManager;
    }

}
