package infuzion.chest.randomizer.util;

import infuzion.chest.randomizer.ChestRandomizer;
import org.bukkit.configuration.file.FileConfiguration;

public class configManager {
    ChestRandomizer pl;
    FileConfiguration config;

    public configManager(ChestRandomizer pl){
        this.pl = pl;
        init();
    }

    private void init(){
        FileConfiguration config = pl.getConfig();

        config.options().header(
                "*******************************#*******************************#" +
                "|-----------------------ChestRandomizer-----------------------|#" +
                "*******************************#*******************************#");

        addDefault("", "jk");
    }

    public void addDefault(String name, Object value){
        config.addDefault("ChestRandomizer." + name, value);
    }
}
