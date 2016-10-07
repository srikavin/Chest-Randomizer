package infuzion.chest.randomizer.util;

import infuzion.chest.randomizer.ChestRandomizer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class configManager {
    private final ChestRandomizer pl;
    private FileConfiguration config;

    public configManager(ChestRandomizer pl) {
        this.pl = pl;
        init();
    }

    private void init() {
        config = pl.getConfig();

        config.options().header(
                        "********************************************************************************************* #\n" +
                        "|---------------------------------------Chest Randomizer------------------------------------| #\n" +
                        "********************************************************************************************* #\n" +
                        "| Format: [Percent] [ItemName]:{Data} {enchant},{enchant2} {lore}  []: Required {}:Optional | #\n" +
                        "| Format: [Percent] [ItemID]:{Data} {enchant},{enchant2} {lore}    []: Required {}:Optional | #\n" +
                        "|                 Example: 48% wool:1 protection:4      48% Chance of wool                  | #\n" +
                        "|   Example: 48% wool:1 none Lore        48% Chance of wool with no enchant and with lore   | #\n" +
                        "|              For Item Id's put it in the ID section, while putting item-names in          | #\n" +
                        "|                                  the item-name section!                                   | #\n" +
                        "|            Refer to: http://www.minecraftinfo.com/idnamelist.htm for names.               | #\n" +
                        "|                Refer to: http://www.minecraftinfo.com/idlist.htm for IDs.                 | #\n" +
                        "********************************************************************************************* #\n");
        addDefault("Version", 2.0f);
        addDefault("Verbose-Output", false);

        addDefault("Metrics.Opt-Out", false);
        addDefault("Updater.Opt-Out", false);

        addDefault("RandomizerSettings.MaximumItems", 10);
        addDefault("RandomizerSettings.MinimumItems", 2);

        ArrayList<String> defaultName = new ArrayList<String>();
        defaultName.add(new configStorageFormat("48% diamond_sword:234 0:15 &4Pretty good sword \\n &5Created in the realm of &2ice \\n &3It is said that the wielder gets stronger").toString());

        addDefault("ByName", defaultName);

        ArrayList<String> defaultID = new ArrayList<String>();
              addDefault("ByID", defaultID);
        config.options().copyDefaults(true);
        pl.saveConfig();

        if (config.getBoolean("ChestRandomizer.Verbose-Output")) {
            for (configStorageFormat e : getAllConfigValues()) {
                pl.getLogger().info(pl.getPrefix() + "Loaded: " + e.toString());
            }
        }
    }

    public List<configStorageFormat> getAllConfigValues() {
        List<String> ls = config.getStringList("ChestRandomizer.ByName");
        List<String> ls2 = config.getStringList("ChestRandomizer.ByID");

        List<configStorageFormat> ret = loadConfigValues(ls);
        ret.addAll(loadConfigValues(ls2));

        return ret;
    }

    private List<configStorageFormat> loadConfigValues(List<String> ls) {
        List<configStorageFormat> returnVal = new ArrayList<configStorageFormat>();
        configStorageFormat cSF;
        for (String i : ls) {
            cSF = new configStorageFormat(i);
            if (!cSF.hasError()) {
                returnVal.add(cSF);
            }
        }
        return returnVal;
    }


    public void addDefault(String name, Object value) {
        config.addDefault("ChestRandomizer." + name, value);
    }

    public void set(String name, Object value) {
        config.set("ChestRandomizer." + name, value);
    }
}
