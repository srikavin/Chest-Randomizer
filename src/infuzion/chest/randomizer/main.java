package src.infuzion.chest.randomizer;

import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin
{
  Logger ConsoleLogger = Bukkit.getLogger();

  public void onEnable() {
    InitialiseConfig();
  }

  private void InitialiseConfig()
  {
    FileConfiguration config = getConfig();
    getConfig().options().header(
      "**********************************************\n*             Chest Randomizer               *\n**********************************************\n* If you are getting errors delete this file *\n**********************************************");

    ItemStack test = new ItemStack(Material.DIAMOND, 5);
    config.addDefault("ChestRandomizer.AmountOfItems", Integer.valueOf(1));
    config.addDefault("ChestRandomizer.1.Items", test);
    config.options().copyDefaults(true);
    saveConfig();
  }

  public boolean onCommand(CommandSender theSender, Command cmd, String commandLabel, String[] args)
  {
    if ((commandLabel.equalsIgnoreCase("chestrandomizer")) || ((commandLabel.equalsIgnoreCase("cr")) && (theSender.hasPermission("cr.access")))) {
      Player thePlayer = (Player)theSender;

      if (args.length == 1) {
        if (args[0].equalsIgnoreCase("Reload")) {
          if (theSender.hasPermission("cr.randomize")) {
            thePlayer.sendMessage(ChatColor.BLUE + "[ChestRandomizer]" + ChatColor.GOLD + "Config files have been reloaded!");
            reloadConfig();
          }
        }
        else if (args[0].equalsIgnoreCase("Randomize"))
          if ((theSender instanceof Player)) {
            if (theSender.hasPermission("cr.randomize")) {
              ItemStack[] Random = new ItemStack[16];
              Random[0] = new ItemStack(Material.DIAMOND, 5);
              Random[1] = new ItemStack(Material.DIAMOND_SWORD, 1);
              Random[2] = new ItemStack(Material.DIAMOND_PICKAXE, 1);
              Random[3] = new ItemStack(Material.COOKED_BEEF, 16);
              Random[4] = new ItemStack(Material.LAVA_BUCKET, 1);
              Random[5] = new ItemStack(Material.WATER_BUCKET, 1);
              Random[6] = new ItemStack(Material.ENCHANTMENT_TABLE, 1);
              Random[7] = new ItemStack(Material.LEATHER_CHESTPLATE);
              Random[8] = new ItemStack(Material.LOG, 25);
              Random[9] = new ItemStack(Material.CHAINMAIL_HELMET, 1);
              Random[10] = new ItemStack(Material.APPLE, 15);
              Random[11] = new ItemStack(Material.STRING, 7);
              Random[12] = new ItemStack(Material.ARROW, 5);
              Random[13] = new ItemStack(Material.GOLDEN_APPLE, 2);
              Random[14] = new ItemStack(Material.ENDER_PEARL, 6);
              Random[15] = new ItemStack(Material.AIR, 1);

              Location pLocation = thePlayer.getLocation();
              pLocation.getBlock().setType(Material.CHEST);
              Chest chest = (Chest)pLocation.getBlock().getState();
              Inventory Inven = chest.getBlockInventory();
              Random rand = new Random();
              int rint = rand.nextInt(15);
              int wint = rand.nextInt(5) + 1;
              while (wint >= 0) {
                Inven.addItem(new ItemStack[] { Random[rint] });
                rint = rand.nextInt(15);
                wint--;
              }
              theSender.sendMessage(ChatColor.GOLD + "[ChestRandomizer]" + ChatColor.GOLD + "Chest has been randomized and placed at feet level");
            }
          } else theSender.sendMessage("This command can only be used by a player!"); 
      }
      else {
        thePlayer.sendMessage(ChatColor.GREEN + 
          "Chest Randomizer Help: \n" + 
          "/cr randomize     Place a randomized chest at feet level \n" + 
          "/cr reload         Reloads the config files                   \n");
      }
    }

    return true;
  }
}