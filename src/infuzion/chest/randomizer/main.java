package src.infuzion.chest.randomizer;

import java.io.File;
import java.io.IOException;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin
{
	Logger ConsoleLogger = Bukkit.getLogger();
	double version = 1.2;
	File rConfigFile;
	FileConfiguration rConfig;	
	String prefix = ChatColor.GREEN + " [" + ChatColor.GRAY + "Chest Randomizer" + ChatColor.GREEN + "] " + ChatColor.WHITE;

	public void onEnable() {
		update();
		InitialiseConfig();
	}
	
	@SuppressWarnings("unused")
	private void update(){
		if(getConfig().getBoolean("ChestRandomizer.Auto-Update")){
			Updater updater = new Updater(this, 83511, this.getFile(), Updater.UpdateType.DEFAULT, false);
		}
		if(getConfig().getBoolean("ChestRandomizer.Metrics")){
			try{Metrics metrics = new Metrics(this);metrics.start();} catch (IOException e) {}
		}
	}
	
	private void InitialiseConfig()
	{
		FileConfiguration config = getConfig();
		getConfig().options().header(""
				+	"**********************************************\n*"
				+	"*	           Chest Randomizer               *\n"
				+ 	"**********************************************\n"
				+	"* If you are getting errors delete this file *\n"
				+	"**********************************************");
		config.addDefault("ChestRandomizer.Auto-Update", true);
		config.addDefault("ChestRandomizer.Metrics", true);
		config.addDefault("ChestRandomizer.AmountOfItems", Integer.valueOf(1));
		config.addDefault("ChestRandomizer.1.Items", new ItemStack(Material.DIAMOND, 5));
		config.options().copyDefaults(true);
		saveConfig();
	}

	private void populateChest(ItemStack i, Location l, boolean run){
		Chest c = (Chest) l.getBlock();
		c.getBlockInventory().addItem(new ItemStack(1, 2));
	}

	private boolean randomize(int percent){
		Random rand = new Random();
		int random = rand.nextInt(100);
		return random <= percent;
	}

	public boolean onCommand(CommandSender theSender, Command cmd, String commandLabel, String[] args)
	{
		if(args.length > 0){

		}else{
			theSender.sendMessage(ChatColor.GREEN + 
					"Chest Randomizer Help: \n" + 
					"/cr randomize     Place a randomized chest at feet level \n" + 
					"/cr reload         Reloads the config files                   \n");
		}
		return true;
	}
}
