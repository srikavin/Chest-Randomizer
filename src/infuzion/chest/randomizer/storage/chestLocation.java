package infuzion.chest.randomizer.storage;

import infuzion.chest.randomizer.util.Direction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("CRChestLocation")
public class chestLocation extends Location implements ConfigurationSerializable {
    private Direction direction;
    private String group = "default";

    public chestLocation(Location loc, Direction dir, String group) {
        super(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
        direction = dir;
        this.group = group;
    }


    public chestLocation(String world, double x, double y, double z, int direction, String group) {
        this(world, x, y, z, Direction.valueOf(direction), group);
    }

    public chestLocation(String world, double x, double y, double z, Direction dir, String group) {
        super(Bukkit.getWorld(world), x, y, z);
        direction = dir;
        this.group = group;
    }

    public static chestLocation deserialize(Map<String, Object> args) {
        String world = (String) args.get("world");
        double x = (Double) args.get("x");
        double y = (Double) args.get("y");
        double z = (Double) args.get("z");
        Direction direction = Direction.valueOf((Integer) args.get("dir"));
        String group = (String) args.get("group");

        return new chestLocation(world, x, y, z, direction, group);
    }

    public int getDir() {
        return direction.getDirection();
    }

    public String getGroup() {
        return group;
    }

    public boolean isInCuboid(Location min, Location max, World world) {
        return this.getWorld().equals(world) && isInCuboid(min, max);
    }

    public boolean isInCuboid(Location min, Location max) {
        return this.toVector().isInAABB(min.toVector(), max.toVector());
    }

    public boolean isInSpheroid(Location location, int radius, World world) {
        return this.getWorld().equals(world) && isInSpheroid(location, radius);
    }

    public boolean isInSpheroid(Location location, int radius) {
        return this.toVector().isInSphere(location.toVector(), radius);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<String, Object>();

        result.put("world", getWorld().getName());
        result.put("x", getX());
        result.put("y", getY());
        result.put("z", getZ());
        result.put("dir", direction.getDirection());
        result.put("group", group);
        return result;
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean similar(Location location) {
        if (location.getBlockX() != this.getBlockX()) {
            return false;
        }
        if (location.getBlockY() != this.getBlockY()) {
            return false;
        }
        if (location.getBlockZ() != this.getBlockZ()) {
            return false;
        }
        return true;
    }
}