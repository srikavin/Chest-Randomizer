package infuzion.chest.randomizer.util.configuration;

import infuzion.chest.randomizer.ChestRandomizer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class configManager {
    private final ChestRandomizer pl;
    private FileConfiguration config;
    private HashMap<String, List<configItemStorageFormat>> groups;

    public configManager(ChestRandomizer pl) {
        this.pl = pl;
        init();
    }

    private void init() {
        config = pl.getConfig();
        groups = new HashMap<String, List<configItemStorageFormat>>();
        if (config.isDouble("ChestRandomizer.Version") && config.getDouble("ChestRandomizer.Version") < 3.0d) {
            updateConfig();
        }
        config.options().header(createHeader(new String[]{"",
                "ChestRandomizer v" + pl.getVersion(),
                "",
                "Format: [Percent] [ItemName]{:Data} {amount} {enchant},{enchant2} {lore}  []: Required {}:Optional",
                " ",
                "Example: 48% wool:1 PROTECTION_ENVIRONMENTAL  48% chance of enchanted wool",
                " ",
                "Refer to: http://www.minecraftinfo.com/idnamelist.htm for item names",
                "Refer to: https://docs.oc.tc/reference/enchantments for enchantment names (use Bukkit name)",
                "",
                "Plugin by: Infuzion",
                ""}));
        addDefault("Version", 3.0f);
        addDefault("Verbose-Output", false);
        boolean firstRun = config.getString("ChestRandomizer.firstrun") == null || !config.getString("ChestRandomizer.firstrun").equals("no");
        addDefault("firstrun", "no");
        addDefault("Metrics.Opt-Out", false);
        addDefault("Updater.Opt-Out", false);

        addDefault("RandomizerSettings.MaximumItems", 10);
        addDefault("RandomizerSettings.MinimumItems", 2);

        if (firstRun) {
            ArrayList<String> defaultGroup = new ArrayList<String>();
            ArrayList<String> groupTwo = new ArrayList<String>();
            defaultGroup.add(new configItemStorageFormat("48% diamond_sword:234 2 0:15 &4Pretty good sword " +
                    "|| &5Created in the realm of &2ice " +
                    "|| &3It is said that the wielder gets stronger").toString());
            groupTwo.add(new configItemStorageFormat("100% diamond_sword:0 3 0:15 This is an example of multiple groups" +
                    "|| Please update your configuration").toString());
            addDefault("Groups.default", defaultGroup);
            addDefault("Groups.grouptwo", groupTwo);
        }
        config.options().copyDefaults(true);
        pl.saveConfig();
        initGroupList();

        if (config.getBoolean("ChestRandomizer.Verbose-Output")) {
            for (configItemStorageFormat e : getAllConfigValues()) {
                pl.getLogger().info(ChatColor.stripColor(pl.getPrefix()) + "Loaded: " + e.toString());
            }
        }
    }

    private void addDefault(String name, Object value) {
        config.addDefault("ChestRandomizer." + name, value);
    }

    public static String createHeader(String strings[]) {
        int longest = 0;
        for (String e : strings) {
            e = e.trim();
            if (e.length() + 2 > longest) {
                longest = e.length() + 2;
            }
        }
        StringBuilder builder = new StringBuilder();

        for (int i1 = 0, stringsLength = strings.length; i1 < stringsLength; i1++) {
            String e = strings[i1];
            if (e.equalsIgnoreCase("")) {
                for (int i = 0; i < longest + 1; i++) {
                    builder.append("*");
                }
                builder.append(" #\n");
            } else if (i1 != 3 && (i1 == 1 || strings[i1 - 1].equalsIgnoreCase(""))) {
                builder.append("|")
                        .append(center(e, longest - 1, '-'))
                        .append("| #\n");
            } else {
                builder.append("| ")
                        .append(center(e, longest - 2, ' '))
                        .append("| #\n");
            }
        }
        return builder.toString();
    }

    private static String center(String s, int size, char pad) {
        if (s == null || size <= s.length()) {
            return s;
        }

        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < (size - s.length()) / 2; i++) {
            sb.append(pad);
        }
        sb.append(s);
        while (sb.length() < size) {
            sb.append(pad);
        }
        return sb.toString();
    }

    private List<configItemStorageFormat> getAllConfigValues() {
        return getAllConfigValues(true);
    }

    private List<configItemStorageFormat> getAllConfigValues(boolean fast) {
        if (fast) {
            List<configItemStorageFormat> toRet = new ArrayList<configItemStorageFormat>();
            for (String e : groups.keySet()) {
                toRet.addAll(groups.get(e));
            }
            return toRet;
        }
        List<String> ls = config.getStringList("ChestRandomizer.Groups");
        config.getConfigurationSection("ChestRandomizer.Groups");
        for (String e : config.getConfigurationSection("ChestRandomizer.Groups").getValues(false).keySet()) {
            for (configItemStorageFormat configItemStorageFormat : getConfigValue(e)) {
                ls.add(configItemStorageFormat.toString());
            }
        }

        return loadConfigValues(ls);
    }

    public List<configItemStorageFormat> getConfigValue(String group) {
        return getConfigValue(group, true);
    }

    private void initGroupList() {
        List<configItemStorageFormat> ls = new ArrayList<configItemStorageFormat>();
        for (String e : config.getConfigurationSection("ChestRandomizer.Groups").getValues(false).keySet()) {
            ls.addAll(getConfigValue(e, false));
            groups.put(e, ls);
            ls = new ArrayList<configItemStorageFormat>();
        }
    }

    private List<configItemStorageFormat> getConfigValue(String group, boolean fast) {
        if (fast) {
            return groups.get(group);
        }
        List<String> list = config.getStringList("ChestRandomizer.Groups." + group);
        List<configItemStorageFormat> toReturn = new ArrayList<configItemStorageFormat>();
        for (String e : list) {
            toReturn.add(new configItemStorageFormat(e));
        }
        return toReturn;
    }

    private void updateConfig() {
        List<String> ls = config.getStringList("ChestRandomizer.ByName");
        List<String> ls2 = config.getStringList("ChestRandomizer.ByID");

        List<configItemStorageFormat> oldConfigValues = oldLoadConfigValues(ls);
        oldConfigValues.addAll(loadConfigValues(ls2));
        File file = new File(pl.getDataFolder().getPath() + File.separator + "config.yml");
        File file2 = new File(pl.getDataFolder().getPath() + File.separator + "config.old.yml");

        pl.getLogger().severe(file.getAbsolutePath());
        try {
            String content = new Scanner(file).useDelimiter("\\Z").next();
            FileWriter fw = new FileWriter(file2);
            fw.write(content);
            fw.close();
            new PrintWriter(file).close();
        } catch (Exception ignore) {
        }
        List<String> updatedConfigValues = new ArrayList<String>();
        for (configItemStorageFormat e : oldConfigValues) {
            updatedConfigValues.add(e.toString());
        }
        config.set("ChestRandomizer.Groups.default", updatedConfigValues);
        config.set("ChestRandomizer.Version", 3.0f);
        config.set("ChestRandomizer.ByName", null);
        config.set("ChestRandomizer.ByID", null);
        pl.getLogger().severe(pl.getPrefix() + "Your config has been updated.");
    }

    private List<configItemStorageFormat> loadConfigValues(List<String> ls) {
        List<configItemStorageFormat> returnVal = new ArrayList<configItemStorageFormat>();
        configItemStorageFormat cSF;
        for (String i : ls) {
            cSF = new configItemStorageFormat(i, true);
            if (!cSF.hasError()) {
                returnVal.add(cSF);
            }
        }
        return returnVal;
    }

    private List<configItemStorageFormat> oldLoadConfigValues(List<String> ls) {
        List<configItemStorageFormat> returnVal = new ArrayList<configItemStorageFormat>();
        configItemStorageFormat cSF;
        for (String i : ls) {
            cSF = new configItemStorageFormat(i);
            if (!cSF.hasError()) {
                returnVal.add(cSF);
            }
        }
        return returnVal;
    }

    public boolean addConfig(configItemStorageFormat configItemStorageFormat) {
        return addConfig(configItemStorageFormat, "default");
    }

    public boolean addConfig(configItemStorageFormat configItemStorageFormat, String group) {
        if (groupExists(group)) {
            List<configItemStorageFormat> configList = getConfigValue(group);
            configList.add(configItemStorageFormat);

            List<String> stringList = new ArrayList<String>();
            for (configItemStorageFormat e : configList) {
                stringList.add(e.toString());
            }
            set("Groups." + group, stringList);
            return true;
        }
        return false;
    }

    public boolean groupExists(String group) {
        return config.getConfigurationSection("ChestRandomizer.Groups").getValues(false).keySet().contains(group);
    }

    public void set(String name, Object value) {
        config.set("ChestRandomizer." + name, value);
        pl.saveConfig();
        this.config = pl.getConfig();
        init();
    }

    public List<String> getGroupNames() {
        List<String> ret = new ArrayList<String>();
        for (String e : groups.keySet()) {
            ret.add(e);
        }
        return ret;
    }

    public void removeGroup(String confirmationGroups) {
        set("Groups." + confirmationGroups, null);
    }
}
