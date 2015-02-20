package infuzion.chest.randomizer.util;

import infuzion.chest.randomizer.ChestRandomizer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class messagesManager {
    ChestRandomizer pl;
    File messagesFile;
    FileConfiguration messagesConfig;

    public messagesManager(ChestRandomizer pl){
        this.pl = pl;
        messagesFile = new File(pl.getDataFolder(), "messages.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        init();
    }

    public enum Messages {
        Prefix,
        ReloadSuccess,
        ServerName
    }

    private void init(){
        messagesConfig.options().header(
                "*******************************#*******************************#" +
                "|-----------------------ChestRandomizer-----------------------|#" +
                "*******************************#*******************************#" +
                "|----------------------Global-Variables:----------------------|#" +
                "*******************************#*******************************#" +
                "|  %prefix% - Adds the customizable prefix in place of this   |#" +
                "| %servername% - Adds the customizable name in place of this  |#" +
                "***************************************************************#");
        addMessage(Messages.Prefix.toString(), "&7[&6Chest-Randomizer&7]&8");
        addMessage(Messages.ServerName.toString(), "&5ServerName");
        addMessage(Messages.ReloadSuccess.toString(),"%prefix% &aPlugin config successfully reloaded!");
    }

    public void addMessage(String name, String value){
        messagesConfig.addDefault("ChestRandomizer.Messages." + name, value);
    }

    public String getMessage(String messageName){
        return parseVariables(messagesConfig.getString("ChestRandomizer.Messages." + messageName));
    }

    public String getMessage(String messageName, boolean parseVariables){
        if(parseVariables){
            return getMessage(messageName);
        }else{
            return messagesConfig.getString("ChestRandomizer.Messages." + messageName);
        }
    }

    public String parseVariables(String input){
        input = input.replace("%prefix%", getMessage("Prefix", false))
                .replace("%servername%", getMessage("ServerName", false));
        return ChatColor.translateAlternateColorCodes('&',input);
    }
}
