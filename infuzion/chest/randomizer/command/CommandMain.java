package infuzion.chest.randomizer.command;

import infuzion.chest.randomizer.ChestRandomizer;
import infuzion.chest.randomizer.util.messagesManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandMain implements CommandExecutor {
    ChestRandomizer pl;
    messagesManager messagesManager;

    public CommandMain(ChestRandomizer pl, messagesManager messagesManager) {
        this.pl = pl;
        this.messagesManager = messagesManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args[0].equalsIgnoreCase("reload")){
            pl.reloadConfig();
            sender.sendMessage("Plugin Configuration Successfully Reloaded");
        }
        return true;
    }
}
