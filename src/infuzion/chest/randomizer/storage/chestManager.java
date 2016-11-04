package infuzion.chest.randomizer.storage;

import infuzion.chest.randomizer.ChestRandomizer;
import infuzion.chest.randomizer.command.CommandMain;
import infuzion.chest.randomizer.util.Direction;
import org.bukkit.Location;

import java.util.*;

public abstract class chestManager {
    final List<chestLocation> chests = Collections.synchronizedList(new LinkedList<chestLocation>());
    protected ChestRandomizer plugin;

    chestManager(ChestRandomizer plugin) {
        this.plugin = plugin;
    }

    public void addChest(Location location, Direction direction, String group) {
        if (containsChest(location)) {
            removeChest(location);
        }
        chests.add(new chestLocation(location, direction, group));
    }

    public boolean containsChest(Location location) {
        synchronized (chests) {
            for (chestLocation e : chests) {
                if (e.similar(location)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void removeChest(Location location) {
        synchronized (chests) {
            Iterator<chestLocation> chestLocationIterator = chests.iterator();
            while (chestLocationIterator.hasNext()) {
                chestLocation loc = chestLocationIterator.next();
                if (loc.similar(location)) {
                    chestLocationIterator.remove();
                    return;
                }
            }
        }
    }

    public abstract void cleanUp();

    public List<chestLocation> getAllChests() {
        return Collections.unmodifiableList(chests);
    }

    public List<chestLocation> getAllChestsInCuboid(Location l1, Location l2) {
        List<chestLocation> ret = new ArrayList<chestLocation>();
        int x1 = Math.min(l1.getBlockX(), l2.getBlockX());
        int y1 = Math.min(l1.getBlockY(), l2.getBlockY());
        int z1 = Math.min(l1.getBlockZ(), l2.getBlockZ());
        int x2 = Math.max(l1.getBlockX(), l2.getBlockX());
        int y2 = Math.max(l1.getBlockY(), l2.getBlockY());
        int z2 = Math.max(l1.getBlockZ(), l2.getBlockZ());
        Location min = new Location(l1.getWorld(), x1, y1, z1);
        Location max = new Location(l2.getWorld(), x2, y2, z2);
        for (chestLocation e : chests) {
            if (e.isInCuboid(min, max)) {
                ret.add(e);
            }
        }
        return ret;
    }

    public List<chestLocation> getAllChestsInGroup(String group) {
        List<chestLocation> ret = new ArrayList<chestLocation>();
        synchronized (chests) {
            for (chestLocation e : chests) {
                if (e.getGroup().equalsIgnoreCase(group)) {
                    ret.add(e);
                }
            }
        }
        return ret;
    }

    public List<chestLocation> getAllChestsInSpheroid(Location center, int radius) {
        List<chestLocation> ret = new ArrayList<chestLocation>();
        synchronized (chests) {
            for (chestLocation e : chests) {
                if (e.isInSpheroid(center, radius)) {
                    ret.add(e);
                }
            }
        }
        return ret;
    }

    public void randomize(chestLocation chestLocation) {
        CommandMain.randomizeChest(chestLocation, chestLocation.getDir(), chestLocation.getGroup(), plugin);
    }

    abstract void save();

}
