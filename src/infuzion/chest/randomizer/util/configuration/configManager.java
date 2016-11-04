package infuzion.chest.randomizer.util.configuration;

import infuzion.chest.randomizer.ChestRandomizer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

public class configManager {
    private final ChestRandomizer pl;
    private FileConfiguration config;
    private Map<String, List<chestRandomizationItem>> groups;

    public configManager(ChestRandomizer pl) {
        this.pl = pl;
        config = pl.getConfig();
        groups = new HashMap<String, List<chestRandomizationItem>>();

        init();
        firstRun();
        initGroupList();

        if (config.getBoolean("ChestRandomizer.Verbose-Output")) {
            for (chestRandomizationItem e : getAllConfigValues()) {
                pl.getLogger().info(ChatColor.stripColor(pl.getPrefix()) + "Loaded: " + e.getItem().getType().name());
            }
        }
    }

    private void firstRun() {
        boolean firstRun = !config.isString("ChestRandomizer.firstrun") || !config.getString("ChestRandomizer.firstrun").equals("no");
        addDefault("firstrun", "no");

        if (firstRun) {
            ArrayList<ConfigurationSerializable> defaultGroup = new ArrayList<ConfigurationSerializable>();
            ArrayList<ConfigurationSerializable> groupTwo = new ArrayList<ConfigurationSerializable>();
            defaultGroup.add(new chestRandomizationItem("48% diamond_sword:234 2 0:15 &4Pretty good sword " +
                    "|| &5Created in the realm of &2ice " +
                    "|| &3It is said that the wielder gets stronger"));
            groupTwo.add(new chestRandomizationItem("100% diamond_sword:0 3 0:15 This is an example of multiple groups" +
                    "|| Please update your configuration"));
            addDefault("Groups.default", defaultGroup);
            addDefault("Groups.grouptwo", groupTwo);
        }
        config.options().copyDefaults(true);
        pl.saveConfig();
    }

    private List<chestRandomizationItem> getAllConfigValues() {
        return getAllConfigValues(true);
    }

    private List<chestRandomizationItem> getAllConfigValues(boolean fast) {
        if (fast) {
            List<chestRandomizationItem> toRet = new ArrayList<chestRandomizationItem>();
            for (String e : groups.keySet()) {
                toRet.addAll(groups.get(e));
            }
            return toRet;
        }
        List<String> ls = config.getStringList("ChestRandomizer.Groups");
        config.getConfigurationSection("ChestRandomizer.Groups");
        for (String e : config.getConfigurationSection("ChestRandomizer.Groups").getValues(false).keySet()) {
            for (chestRandomizationItem chestRandomizationItem : getConfigValue(e)) {
                ls.add(chestRandomizationItem.toString());
            }
        }

        return loadConfigValues(ls);
    }

    public List<chestRandomizationItem> getConfigValue(String group) {
        return getConfigValue(group, true);
    }

    private void init() {
        if (config.isDouble("ChestRandomizer.Version")) {
            double version = config.getDouble("ChestRandomizer.Version");
            if (version < 3.0d) {
                updateConfig30();
                updateConfig35();
            } else if (version < 3.5d) {
                updateConfig35();
            }
        }
        config.options().header(createHeader(new String[]{"",
                "ChestRandomizer v" + pl.getVersion(),
                "",
                "Refer to: http://www.minecraftinfo.com/idnamelist.htm for item names",
                "Refer to: https://docs.oc.tc/reference/enchantments for enchantment names (use Bukkit name)",
                "",
                "Plugin by: Infuzion",
                ""}));
        addDefault("Version", 3.0f);
        addDefault("Verbose-Output", false);

        addDefault("Metrics.Opt-Out", false);
        addDefault("Updater.Opt-Out", false);

        addDefault("RandomizerSettings.MaximumItems", 10);
        addDefault("RandomizerSettings.MinimumItems", 2);
        addDefault("RemoveChestOnBreak", true);
        addDefault("disableAutoBackup", false);
        addDefault("DataBase.using", false);
        addDefault("DataBase.host", "127.0.0.1");
        addDefault("DataBase.port", "3306");
        addDefault("DataBase.user", "user");
        addDefault("DataBase.pass", "pass");
        addDefault("DataBase.database", "dbname");
        addDefault("DataBase.tableName", "tablename");
        addDefault("DataBase.driver", "com.mysql.jdbc.Driver");

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

    private void addDefault(String name, Object value) {
        config.addDefault("ChestRandomizer." + name, value);
    }

    private void updateConfig30() {
        List<String> ls = config.getStringList("ChestRandomizer.ByName");
        List<String> ls2 = config.getStringList("ChestRandomizer.ByID");

        List<chestRandomizationItem> oldConfigValues = oldLoadConfigValues(ls);
        oldConfigValues.addAll(loadConfigValues(ls2));
        File file = new File(pl.getDataFolder().getPath() + File.separator + "config.yml");
        File file2 = new File(pl.getDataFolder().getPath() + File.separator + "config.old.yml");

        try {
            String content = new Scanner(file).useDelimiter("\\Z").next();
            FileWriter fw = new FileWriter(file2);
            fw.write(content);
            fw.close();
            new PrintWriter(file).close();
        } catch (Exception ignore) {
        }
        List<String> updatedConfigValues = new ArrayList<String>();
        for (chestRandomizationItem e : oldConfigValues) {
            updatedConfigValues.add(e.toString());
        }
        config.set("ChestRandomizer.Groups.default", updatedConfigValues);
        config.set("ChestRandomizer.Version", 3.0f);
        config.set("ChestRandomizer.ByName", null);
        config.set("ChestRandomizer.ByID", null);
        pl.getLogger().severe(pl.getPrefix() + "Your config has been updated to config v.3.0");
        pl.saveConfig();
    }

    private List<chestRandomizationItem> loadConfigValues(List<String> ls) {
        List<chestRandomizationItem> returnVal = new ArrayList<chestRandomizationItem>();
        chestRandomizationItem cSF;
        for (String i : ls) {
            cSF = new chestRandomizationItem(i, true);
            if (!cSF.hasError()) {
                returnVal.add(cSF);
            }
        }
        return returnVal;
    }

    private List<chestRandomizationItem> oldLoadConfigValues(List<String> ls) {
        List<chestRandomizationItem> returnVal = new ArrayList<chestRandomizationItem>();
        chestRandomizationItem cSF;
        for (String i : ls) {
            cSF = new chestRandomizationItem(i);
            if (!cSF.hasError()) {
                returnVal.add(cSF);
            }
        }
        return returnVal;
    }

    private void updateConfig35() {
        Map<String, List<chestRandomizationItem>> map = new HashMap<String, List<chestRandomizationItem>>();
        config.set("ChestRandomizer.Version", 3.5f);
        config.getConfigurationSection("ChestRandomizer.Groups");
        for (String e : config.getConfigurationSection("ChestRandomizer.Groups").getValues(false).keySet()) {
            List<chestRandomizationItem> ls = new ArrayList<chestRandomizationItem>();
            for (chestRandomizationItem chestRandomizationItem : getConfigValue(false, e)) {
                ls.add(chestRandomizationItem);
            }
            map.put(e, ls);
        }

        for (Map.Entry<String, List<chestRandomizationItem>> e : map.entrySet()) {
            config.set("ChestRandomizer.Groups." + e.getKey(), e.getValue());
        }
        pl.getLogger().severe(pl.getPrefix() + "Your config has been updated to config v.3.5");
        pl.saveConfig();
    }

    private List<chestRandomizationItem> getConfigValue(boolean old, String group) {
        List<?> list = config.getList("ChestRandomizer.Groups." + group);
        List<chestRandomizationItem> toReturn = new ArrayList<chestRandomizationItem>();
        for (Object e : list) {
            if (e instanceof String) {
                String string = (String) e;
                toReturn.add(new chestRandomizationItem(string));
            } else if (e instanceof chestRandomizationItem) {
                chestRandomizationItem format = (chestRandomizationItem) e;
                toReturn.add(format);
            }
        }
        return toReturn;
    }

    private void initGroupList() {
        List<chestRandomizationItem> ls = new ArrayList<chestRandomizationItem>();
        for (String e : config.getConfigurationSection("ChestRandomizer.Groups").getValues(false).keySet()) {
            ls.addAll(getConfigValue(e, false));
            groups.put(e, ls);
            ls = new ArrayList<chestRandomizationItem>();
        }
    }

    private List<chestRandomizationItem> getConfigValue(String group, boolean fast) {
        if (fast) {
            return groups.get(group);
        }
        List<chestRandomizationItem> list = (List<chestRandomizationItem>) config.getList("ChestRandomizer.Groups." + group);
        List<chestRandomizationItem> toReturn = new ArrayList<chestRandomizationItem>();
        for (chestRandomizationItem e : list) {
            toReturn.add(e);
        }
        return toReturn;
    }

    public boolean addConfig(chestRandomizationItem chestRandomizationItem) {
        return addConfig(chestRandomizationItem, "default");
    }

    public boolean addConfig(chestRandomizationItem chestRandomizationItem, String group) {
        if (groupExists(group)) {
            List<chestRandomizationItem> configList = getConfigValue(group);
            configList.add(chestRandomizationItem);

            List<chestRandomizationItem> itemStorageFormats = new ArrayList<chestRandomizationItem>();
            for (chestRandomizationItem e : configList) {
                itemStorageFormats.add(e);
            }
            set("Groups." + group, itemStorageFormats);
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
    }

    public boolean getBoolean(String string) {
        return config.getBoolean("ChestRandomizer." + string);
    }

    public List<String> getGroupNames() {
        List<String> ret = new ArrayList<String>();
        for (String e : groups.keySet()) {
            ret.add(e);
        }
        return ret;
    }

    public void reload() {
        config = pl.getConfig();
        initGroupList();
    }

    public void removeGroup(String confirmationGroups) {
        set("Groups." + confirmationGroups, null);
    }
}
