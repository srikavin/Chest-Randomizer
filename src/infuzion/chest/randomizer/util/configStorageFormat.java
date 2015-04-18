package infuzion.chest.randomizer.util;

import infuzion.chest.randomizer.ChestRandomizer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class configStorageFormat {
    private final String configValue;
    private final String itemID;
    private Material item;
    private int percent = 0;
    private boolean error = false;
    /*
        Planned:
    private String lore;
    private String name;
    private int declared = 0;
    */

    public configStorageFormat (String configValue) {
        String[] split;
        this.configValue = configValue.trim();

        split = configValue.trim().split(" ", 2);
        try {
            percent = Integer.parseInt(split[0].replace("%", "").trim());
        } catch (NumberFormatException e) {
            error = true;
            ChestRandomizer.getPlugin(ChestRandomizer.class).getLogger().severe("Failed to read number in config: " + split[0].trim());
        }
        /*if (!error) {
            //declared = 1;
        } else {
            return;
        }*/
        itemID = split[1].trim();
        item = Material.matchMaterial(itemID);
        if (item == null) {
            // declared = 2;
        //} else {
            try {
                item = Material.getMaterial(Integer.parseInt(split[1]));
            } catch (NumberFormatException e) {
                error = true;
                ChestRandomizer.getPlugin(ChestRandomizer.class).getLogger().severe("Failed to read item name in config: " + split[1].trim());
            }
        }

        /*
        Planned
        if (split.length > 2) {
            name = split[2];
        }
        */
    }


    public configStorageFormat (String item, int percent) {
        configValue = percent + "% " + item;
        itemID = item;
        this.percent = percent;
    }

    //Planned:
    /*
    public configStorageFormat (String item, int percent, String name) {
        configValue = percent + "% " + item + " " + name + "%en% ";
        itemID = item;
        this.percent = percent;
        this.name = name;
    }

    public configStorageFormat (String item, int percent, String name, String lore) {
        configValue = percent + "% " + item + " " + name + "%en% " + lore + "%el%";
        itemID = item;
        this.percent = percent;
        this.name = name;
        String s = this.lore = lore;
    }
    */

    public ItemStack getItem () {
        return new ItemStack(item);
    }

    @Override
    public String toString () {
        return configValue;
    }

    public int getPercent () {
        return percent;
    }

    /*public String getItemID () {
        return itemID;
    }*/

    /*
    Planned:
    public String getName () {
        return name;
    }
    */

    public Boolean hasErrored () {
        return error;
    }

    /*public int getDeclared () {
        return declared;
    }
    */
}
