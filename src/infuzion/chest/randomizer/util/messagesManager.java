package infuzion.chest.randomizer.util;

import infuzion.chest.randomizer.ChestRandomizer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class messagesManager {

    ChestRandomizer pl;
    File messagesFile;
    FileConfiguration messagesConfig;

    public messagesManager (ChestRandomizer pl) {
        this.pl = pl;
        messagesFile = new File (pl.getDataFolder (), "messages.yml");
        messagesConfig = YamlConfiguration.loadConfiguration (messagesFile);
        init ();
    }

    private void init () {
        messagesConfig.options ().header (
                "*******************************#******************************* #\n" +
                        "|-----------------------ChestRandomizer-----------------------| #\n" +
                        "*******************************#******************************* #\n" +
                        "|----------------------Global-Variables:----------------------| #\n" +
                        "*******************************#******************************* #\n" +
                        "|  %prefix% - Adds the customizable prefix in place of this   | #\n" +
                        "| %servername% - Adds the customizable name in place of this  | #\n" +
                        "*************************************************************** #\n");
        addMessage ("Variables.Prefix", "&7[&6Chest-Randomizer&7]&8");
        addMessage ("Variables.ServerName", "&5ServerName");
        addMessage ("ReloadSuccess", "%prefix% &aPlugin config successfully reloaded!");
        messagesConfig.options ().copyDefaults (true);
        try {
            messagesConfig.save (messagesFile);
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

    public void addMessage (String name, String value) {
        messagesConfig.addDefault ("ChestRandomizer.Messages." + name, value);
    }

    public String getMessage (String messageName) {
        return parseVariables (messagesConfig.getString ("ChestRandomizer.Messages." + messageName));
    }

    public String getMessage (String messageName, boolean parseVariables) {
        if (parseVariables) {
            return getMessage (messageName);
        } else {
            return messagesConfig.getString ("ChestRandomizer.Messages." + messageName);
        }
    }

    public String parseVariables (String input) {
        input = input.replace ("%prefix%", getMessage ("Variables.Prefix", false))
                .replace ("%servername%", getMessage ("Variables.ServerName", false));
        return ChatColor.translateAlternateColorCodes ('&', input);
    }
}
