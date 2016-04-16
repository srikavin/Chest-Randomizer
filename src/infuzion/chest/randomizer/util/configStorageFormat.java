package infuzion.chest.randomizer.util;

import infuzion.chest.randomizer.ChestRandomizer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class configStorageFormat {
    private final String configValue;
    private String itemID;
    private Material item;
    private short data = 0;
    private int percent = 0;
    private boolean error = false;
    private String lore;
    private ItemStack itemstack;
    private Map<Enchantment, Integer> enchantmentMap = new HashMap<Enchantment, Integer>();

    public configStorageFormat(String configValue) {
        String[] split;
        this.configValue = configValue.trim();

        //split[0] = percent
        //split[1] = name:data
        //split[2] = enchants
        //split[3] = lore
        split = configValue.trim().split(" ", 4); //[percent]%, [name]:[data], [enchant], [lore]
        try {
            percent = Integer.parseInt(split[0].replace("%", "").trim()); //[percent]
        } catch (NumberFormatException e) {
            error = true;
            ChestRandomizer.getPlugin(ChestRandomizer.class).getLogger().severe("Failed to read number in config: " + split[0].trim());
        }
        itemID = split[1].trim(); //[name]{:[data]}
        if (itemID.contains(":")) {
            try {
                data = Short.parseShort(itemID.split(":")[1]); //[data]
            } catch (NumberFormatException e) {
                error = true;
                ChestRandomizer.getPlugin(ChestRandomizer.class).getLogger().severe("Failed to read item data in config: " + itemID.split(":")[1]);
            }
            itemID = itemID.split(":")[0]; //[name]
        }

        item = Material.matchMaterial(itemID);
        if (item == null) {
            try {
                item = Material.getMaterial(Integer.parseInt(split[1]));
            } catch (NumberFormatException e) {
                error = true;
                ChestRandomizer.getPlugin(ChestRandomizer.class).getLogger().severe("Failed to read item name in config: " + split[1].trim());
            }
        }
        itemstack = new ItemStack(item, 1, data);
        if (split.length > 2) {
            String[] enchantmentSplit = split[2].split(",", 0); // enchant1,enchant2 -> [enchantname],[lvl]
            int enchantmentLevel;
            Enchantment enchantment;
            int enchantmentID;
            for (String e : enchantmentSplit) {
                try {
                    enchantmentLevel = Integer.parseInt(e.split(":", 2)[1]);
                    enchantment = Enchantment.getByName(e.split(":", 2)[0].toUpperCase());
                    if (enchantment == null && !e.split(":", 2)[0].equalsIgnoreCase("none")) {
                        enchantmentID = Integer.parseInt(e.split(":", 2)[0]);
                        enchantment = Enchantment.getById(enchantmentID);
                    }
                    enchantmentMap.put(enchantment, enchantmentLevel);
                } catch (NumberFormatException err) {
                    ChestRandomizer.getPlugin(ChestRandomizer.class).getLogger().severe("Failed to read item enchant in config: " + e);
                }

            }
            if (!enchantmentMap.isEmpty()) {
                itemstack.addUnsafeEnchantments(enchantmentMap);
            }
            if (split.length > 3) {
                lore = split[3];
                ItemMeta loreMeta = itemstack.getItemMeta();
                loreMeta.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', lore).split("\\n")));
                itemstack.setItemMeta(loreMeta);
            }
        }

    }

    private void generateItem(){

    }
    //public configStorageFormat(String )

    public configStorageFormat(String item, int percent) {
        configValue = percent + "% " + item;
        itemID = item;
        this.percent = percent;
    }

    public ItemStack getItem() {
        return itemstack;
    }

    @Override
    public String toString() {
        return configValue;
    }

    public int getPercent() {
        return percent;
    }

    public Boolean hasErrored() {
        return error;
    }
}
