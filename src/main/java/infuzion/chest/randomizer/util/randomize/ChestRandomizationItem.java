package infuzion.chest.randomizer.util.randomize;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SerializableAs("ChestRandomizationItem")
public class ChestRandomizationItem implements ConfigurationSerializable {
    private static final Pattern LORE_LINE_SPLIT = Pattern.compile("\\|\\|");
    private static final Pattern COLOR_CHARACTER = Pattern.compile("§");
    private static final Pattern PERCENTAGE_CHARACTER = Pattern.compile("%", Pattern.LITERAL);
    private String configValue;
    private Material item;
    private short data = 0;
    private double percent = 0;
    private boolean error = false;
    private int amount = 1;
    private String lore = "";
    private ItemStack itemstack;
    private String itemName = "";
    private Map<Enchantment, Integer> enchantmentMap = new HashMap<>();


    public ChestRandomizationItem(ItemStack itemStack, double percent) {
        this.itemstack = itemStack;
        this.percent = percent;
        ItemMeta itemMeta = itemStack.getItemMeta();

        this.item = itemStack.getType();
        this.data = itemStack.getDurability();

        this.enchantmentMap = itemStack.getEnchantments();

        StringBuilder loreBuffer = new StringBuilder();
        if (itemMeta.hasLore()) {
            for (String e : itemMeta.getLore()) {
                loreBuffer.append(COLOR_CHARACTER.matcher(e).replaceAll("&")).append("||");
            }
        }

        this.lore = loreBuffer.toString();

        setConfigValue();
    }

    public ChestRandomizationItem(Map data) {
        if (data.get("percent") instanceof Integer) {
            this.percent = (Integer) data.get("percent");
        } else {
            this.percent = (Double) data.get("percent");
        }
        this.itemName = (String) data.get("name");
        this.lore = (String) data.get("lore");
        this.data = Short.parseShort(String.valueOf(data.get("data")));
        this.item = Material.valueOf(String.valueOf(data.get("material")));
        this.amount = (Integer) data.get("amount");
        addEnchantments((String) data.get("enchantments"));
        generateItem();
    }

    public ChestRandomizationItem(String configValue) {
        this(configValue, false);
    }

    public ChestRandomizationItem(String configValue, boolean old) {
        String[] split = configValue.trim().split(" ", 5);
        if (split.length < 2) {
            Bukkit.getLogger().severe("Failed to read config line: " + configValue);
        }

        //split[0] = percent
        //split[1] = name:data
        //split[2] = amount
        //split[3] = enchants
        //split[4] = lore
        String sPercent = split[0].trim();
        String sNameData = split[1].trim();
        String sAmount = "1";
        String sEnchants = "none";
        String sLore = "";
        if (split.length > 2) {
            sAmount = split[2].trim();
        }
        if (split.length > 3) {
            sEnchants = split[3].trim();
        }
        if (split.length > 4) {
            sLore = split[4].trim();
        }
        if (old) {
            sAmount = "1";
            if (split.length > 3) {
                sEnchants = split[2].trim();
            }
            if (split.length > 4) {
                sLore = split[3].trim();
            }
        }

        String sData;
        String sName;

        if (sNameData.contains(":")) {
            String[] saNameData = sNameData.split(":"); //[name]:[data]
            sData = saNameData[1];
            sName = saNameData[0];
        } else {
            sData = "0";
            sName = sNameData;
        }

        try {
            percent = Integer.parseInt(PERCENTAGE_CHARACTER.matcher(sPercent).replaceAll(Matcher.quoteReplacement(""))); //[percent]
        } catch (NumberFormatException e) {
            Bukkit.getLogger().severe("Failed to read percent in config: " + sPercent);
            error = true;
            return;
        }

        try {
            data = Short.parseShort(sData); //[data]
        } catch (NumberFormatException e) {
            Bukkit.getLogger().severe("Failed to read item data in config: " + sData);
            error = true;
            return;
        }

        item = Material.matchMaterial(sName); //[name]
        if (item == null) {
            Bukkit.getLogger().severe("Failed to read item name in config: " + sName);
            error = true;
            return;
        }

        addAmount(sAmount);
        addEnchantments(sEnchants);
        addLore(sLore);

        generateItem();
        setConfigValue();
    }

    @SuppressWarnings("unused")
    public static ChestRandomizationItem deserialize(Map data) {
        return new ChestRandomizationItem(data);
    }

    private void setConfigValue() {
        generateItem();
        ItemMeta itemMeta = this.itemstack.getItemMeta();

        StringBuilder loreToReturn = new StringBuilder();
        if (itemMeta.hasLore()) {
            for (String e : itemMeta.getLore()) {
                loreToReturn.append(COLOR_CHARACTER.matcher(e).replaceAll("&")).append("||");

            }
        }

        this.configValue = percent + "% " + item.toString() + ':' + data + ' ' + amount + ' ' + getEnchantmentsAsString() + ' ' + loreToReturn;
    }

    private String getEnchantmentsAsString() {
        StringBuilder enchantments = new StringBuilder();
        for (Enchantment e : enchantmentMap.keySet()) {
            if (enchantments.length() == 0) {
                enchantments.append(e.getName()).append(':').append(enchantmentMap.get(e));

            } else {
                enchantments.append(',').append(e.getName()).append(':').append(enchantmentMap.get(e));
            }
        }

        String toRet;
        if (enchantments.length() == 0) {
            toRet = "none";
        } else {
            toRet = enchantments.toString();
        }
        return toRet;
    }

    private void generateItem() {
        itemstack = new ItemStack(item, amount, data);
        itemstack.addUnsafeEnchantments(enchantmentMap);
        ItemMeta itemMeta = itemstack.getItemMeta();
        String[] loreArray = LORE_LINE_SPLIT.split(lore);
        List<String> loreList = new ArrayList<>();
        for (String e : loreArray) {
            loreList.add(ChatColor.translateAlternateColorCodes('&', e).trim());
        }
        itemMeta.setLore(loreList);
        itemMeta.setDisplayName(itemName);
        itemstack.setItemMeta(itemMeta);
    }

    private void addEnchantments(String enchantments) {
        if (enchantments.equalsIgnoreCase("none")) {
            return;
        }

        int enchantmentLevel;
        Enchantment enchantment;
        int enchantmentID;

        String[] enchantmentSplit = enchantments.split(",", 0); // enchant1,enchant2 -> [enchantName],[lvl]
        for (String e : enchantmentSplit) {
            try {
                if (e.split(":", 2).length != 2) {
                    throw new ArrayIndexOutOfBoundsException();
                }

                enchantment = Enchantment.getByName(e.split(":", 2)[0].toUpperCase());
                enchantmentLevel = Integer.parseInt(e.split(":", 2)[1]);

                if (enchantment == null) {
                    enchantmentID = Integer.parseInt(e.split(":", 2)[0]);
                    enchantment = new EnchantmentWrapper(enchantmentID);
                }
                enchantmentMap.put(enchantment, enchantmentLevel);
            } catch (Exception err) {
                Bukkit.getLogger().severe("Failed to read item enchant in config: " + e);
                return;
            }

        }
    }

    private void addAmount(String amount) {
        try {
            this.amount = Integer.parseInt(amount);
        } catch (Exception e) {
            this.amount = 1;
        }
    }

    private void addLore(String lore) {
        this.lore = lore;
    }

    public ItemStack getItem() {
        return itemstack;
    }

    public double getPercent() {
        return percent;
    }

    public boolean hasError() {
        return error;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        generateItem();
        map.put("name", itemName);
        map.put("material", item.toString());
        map.put("data", data);
        map.put("lore", lore);
        map.put("amount", amount);

        map.put("enchantments", getEnchantmentsAsString());
        map.put("percent", percent);
        return map;
    }

    @Override
    public String toString() {
        return configValue;
    }
}
