package infuzion.chest.randomizer.event;

import infuzion.chest.randomizer.ChestRandomizer;
import infuzion.chest.randomizer.storage.ChestManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class onBlockBreak implements Listener {
    private ChestRandomizer plugin;
    private ChestManager chestManager;

    public onBlockBreak(ChestRandomizer plugin) {
        this.plugin = plugin;
        chestManager = plugin.getChestManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreakEvent(BlockBreakEvent e) {
        if (!e.isCancelled() && e.getBlock().getType() == Material.CHEST) {
            if (plugin.getConfigManager().getBoolean("RemoveChestOnBreak")) {
                if (plugin.getConfigManager().getBoolean("RequirePermissionOnBreak")) {
                    String perm = plugin.getConfigManager().getString("PermissionOnBreak");
                    if (!e.getPlayer().hasPermission(perm)) {
                        return;
                    }
                }
                if (chestManager.containsChest(e.getBlock().getLocation())) {
                    chestManager.removeChest(e.getBlock().getLocation());
                }
            }
        }
    }
}
