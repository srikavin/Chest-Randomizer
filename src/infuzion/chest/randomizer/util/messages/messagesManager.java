package infuzion.chest.randomizer.util.messages;

import infuzion.chest.randomizer.ChestRandomizer;
import infuzion.chest.randomizer.util.configuration.configManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class messagesManager {

    private final ChestRandomizer pl;
    private File messagesFile;
    private FileConfiguration messagesConfig;

    public messagesManager (ChestRandomizer pl) {
        this.pl = pl;
        messagesFile = new File(pl.getDataFolder(), "messages.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        init();
    }

    private void init () {
        messagesConfig.options().header(configManager.createHeader(new String[]{"",
                "ChestRandomizer v" + pl.getVersion() + " Message Config:",
                "%prefix% - Adds the customizable prefix in place of this",
                "%servername% - Adds the customizable name in place of this",
                "",
                "Plugin by: Infuzion",
                ""}));
        addMessage("Variables.Prefix", "&7[&6Chest-Randomizer&7]&8");
        addMessage("Variables.ServerName", "&5ServerName");
        addMessage("ChestRandomizationError.Direction", "%prefix% &4Invalid Direction!");
        addMessage("ChestRandomizationError.Number", "%prefix% &4Invalid Value!");
        addMessage("ChestRandomizationError.Permission", "%prefix% &4You do not have permission to do this!");
        addMessage("ChestRandomizationError.Player", "%prefix% &4You must be in-game to do this!");
        addMessage("ChestRandomizationError.Unknown", "%prefix% &4An unknown error has occurred!");
        addMessage("ChestRandomizationError.Group", "%prefix% &4Randomization group not found!");
        addMessage("ChestRandomizationError.Message", "%prefix% &4Message not found!");
        addMessage("ChestRandomizationError.World", "%prefix% &4World not found!");
        addMessage("Reload.Success", "%prefix% &aPlugin config successfully reloaded!");
        addMessage("Metrics.OptOut", "%prefix% Metrics had been &4disabled");
        addMessage("Metrics.OptIn", "%prefix% Metrics had been &aensabled");
        addMessage("Updater.OptOut", "%prefix% Auto-Updater had been &4disabled");
        addMessage("Updater.OptIn", "%prefix% Auto-Updater had been &aensabled");
        addMessage("Randomize.Success", "%prefix% &aA chest has been placed successfully.");
        addMessage("Admin.Help", "%prefix% &4Usage: /cr admin [help/add/remove/create]");
        addMessage("Admin.Add.Help", "%prefix% &4Usage: /cr admin add <percent> [group]");
        addMessage("Admin.Remove.Help", "%prefix% &4Usage: /cr admin remove [group]");
        addMessage("Admin.Remove.Prompt", "%prefix% &2Type /confirm to confirm this action. This will time out in 30 seconds.");
        addMessage("Admin.Remove.Success", "%prefix% &aSuccessfully removed group: <group>.");
        addMessage("Admin.Remove.Timeout", "%prefix% &4Your request has timed out. Group <group> has not been deleted.");
        addMessage("Admin.Add.Success", "%prefix% &aSuccessfully added the item!");
        addMessage("Admin.Add.NoItem", "%prefix% &4You have no item in your main hand!");
        addMessage("Admin.Create.Help", "%prefix% &4Usage /cr admin create [groupname]");
        addMessage("Admin.Create.Exists", "%prefix% &4The group you specified exists!");
        addMessage("Admin.Create.Success", "%prefix% &aSuccessfully created the group!");
        addMessage("Help.Help", "%prefix% Plugin help:");
        addMessage("Help.Reload", "%prefix% &a/cr reload      &bReloads the plugin");
        addMessage("Help.Randomize", "%prefix% &a/cr randomize   &bRandomizes a chest");
        addMessage("Help.Admin", "%prefix% &a/cr admin       &bPerforms admin operations");
        addMessage("Help.Empty", "%prefix% ChestRandomizer v" + pl.getVersion() + " Made by Infuzion");

        messagesConfig.options().copyDefaults(true);
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addMessage(String name, String value) {
        messagesConfig.addDefault("ChestRandomizer.Messages." + name, value);
    }

    String getMessage(String messageName) {
        String message = messagesConfig.getString("ChestRandomizer.Messages." + messageName);
        if (message != null && !message.equals("")) {
            return parseVariables(message);
        } else {
            if (messageName.equals("ChestRandomizationError.Message")) {
                return parseVariables("%prefix% &4Message not found!");
            }
            return getMessage("ChestRandomizationError.Message");
        }
    }

    private String getMessage(String messageName, boolean parseVariables) {
        if (parseVariables) {
            return getMessage(messageName);
        } else {
            return messagesConfig.getString("ChestRandomizer.Messages." + messageName);
        }
    }

    private String parseVariables(String input) {
        input = input.replace("%prefix%", getMessage("Variables.Prefix", false))
                .replace("%servername%", getMessage("Variables.ServerName", false));
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
