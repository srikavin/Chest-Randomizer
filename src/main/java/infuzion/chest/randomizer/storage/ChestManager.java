package infuzion.chest.randomizer.storage;

import infuzion.chest.randomizer.ChestRandomizer;
import infuzion.chest.randomizer.command.CommandMain;
import infuzion.chest.randomizer.util.RandomizationGroup;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ChestManager {
    final List<ChestLocation> chests = new CopyOnWriteArrayList<>();
    protected ChestRandomizer plugin;

    ChestManager(ChestRandomizer plugin) {
        this.plugin = plugin;
    }

    public void addChest(Location location, BlockFace direction, String group) {
        if (containsChest(location)) {
            removeChest(location);
        }
        chests.add(new ChestLocation(location, direction, group));
    }

    public boolean containsChest(Location location) {
        for (ChestLocation e : chests) {
            if (e.similar(location)) {
                return true;
            }
        }
        return false;
    }

    public void removeChest(Location location) {
        ChestLocation toRemove = null;
        for (ChestLocation loc : chests) {
            if (loc.similar(location)) {
                toRemove = loc;
                break;
            }
        }
        if (toRemove == null) {
            return;
        }
        chests.remove(toRemove);
    }

    public abstract void cleanUp();

    public List<ChestLocation> getAllChests() {
        return Collections.unmodifiableList(chests);
    }

    public List<ChestLocation> getAllChestsInCuboid(Location l1, Location l2) {
        List<ChestLocation> ret = new ArrayList<>();
        int x1 = Math.min(l1.getBlockX(), l2.getBlockX());
        int y1 = Math.min(l1.getBlockY(), l2.getBlockY());
        int z1 = Math.min(l1.getBlockZ(), l2.getBlockZ());
        int x2 = Math.max(l1.getBlockX(), l2.getBlockX());
        int y2 = Math.max(l1.getBlockY(), l2.getBlockY());
        int z2 = Math.max(l1.getBlockZ(), l2.getBlockZ());
        Location min = new Location(l1.getWorld(), x1, y1, z1);
        Location max = new Location(l2.getWorld(), x2, y2, z2);
        for (ChestLocation e : chests) {
            if (e.isInCuboid(min, max)) {
                ret.add(e);
            }
        }
        return ret;
    }

    public List<ChestLocation> getAllChestsInGroup(RandomizationGroup group) {
        List<ChestLocation> ret = new ArrayList<>();
        for (ChestLocation e : chests) {
            if (e.getGroup().equals(group)) {
                ret.add(e);
            }
        }
        return ret;
    }

    public List<ChestLocation> getAllChestsInSpheroid(Location center, int radius) {
        List<ChestLocation> ret = new ArrayList<>();
        for (ChestLocation e : chests) {
            if (e.isInSpheroid(center, radius)) {
                ret.add(e);
            }
        }
        return ret;
    }

    public void randomize(ChestLocation chestLocation) {
        CommandMain.randomizeChest(chestLocation, chestLocation.getDir(), chestLocation.getGroup(), plugin);
    }

    abstract void save();

}
