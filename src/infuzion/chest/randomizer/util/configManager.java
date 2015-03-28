package infuzion.chest.randomizer.util;

import infuzion.chest.randomizer.ChestRandomizer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class configManager {
    ChestRandomizer pl;
    FileConfiguration config;

    public configManager(ChestRandomizer pl) {
        this.pl = pl;
        init();
    }

    private void init() {
        config = pl.getConfig();

        config.options().header(
                "**************************************#*************************************** #\n" +
                        "|-------------------------------ChestRandomizer------------------------------| #\n" +
                        "**************************************#*************************************** #\n" +
                        "|Format: [Percent] [ItemName]:{Data} {Name} {Lore}  []: Required {}:Optional | #\n" +
                        "|Format: [Percent] [ItemName]:{Data} {Name}  []: Required {}:Optional        | #\n" +
                        "|      Example: 48% wool:1                   48% Chance of wool              | #\n" +
                        "| For Item Id's put it in the ID section, while putting item-names in        | #\n" +
                        "|                        the item-name section!                              | #\n" +
                        "|  Refer to: http://www.minecraftinfo.com/idnamelist.htm for names.          | #\n" +
                        "|     Refer to: http://www.minecraftinfo.com/idlist.htm for IDs.             | #\n" +
                        "**************************************#*************************************** #\n");
        addDefault("Version", 2.0f);
        addDefault("Verbose-Output", false);

        addDefault("Metrics.Opt-Out", false);
        addDefault("Updater.Opt-Out", false);

        addDefault("RandomizerSettings.MaximumItems", 10);
        addDefault("RandomizerSettings.MinimumItems", 2);

        ArrayList<String> defaultName = new ArrayList<String>();
        defaultName.add(new configStorageFormat("redstone_block", 48).toString());
        addDefault("ByName", defaultName);

        ArrayList<String> defaultID = new ArrayList<String>();
        defaultID.add(new configStorageFormat("152", 68).toString());
        addDefault("ByID", defaultID
        );
        config.options().copyDefaults(true);
        pl.saveConfig();

        if (config.getBoolean("ChestRandomizer.Verbose-Output")) {
            for (configStorageFormat e : getAllConfigValues()) {
                pl.getLogger().severe(pl.getPrefix() + "Loaded: " + e.toString());
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
            if (!cSF.hasErrored()) {
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
