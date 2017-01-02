package infuzion.chest.randomizer.storage;

import infuzion.chest.randomizer.util.BlockFaceWrapper;
import infuzion.chest.randomizer.util.Direction;
import infuzion.chest.randomizer.util.RandomizationGroup;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("CRChestLocation")
public class ChestLocation extends Location implements ConfigurationSerializable {
    private BlockFace direction;
    private RandomizationGroup group;

    public ChestLocation(Location loc, BlockFace dir, String group) {
        super(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
        direction = dir;
        this.group = RandomizationGroup.getGroup(group);
    }


    public ChestLocation(String world, double x, double y, double z, BlockFace dir, RandomizationGroup group) {
        super(Bukkit.getWorld(world), x, y, z);
        direction = dir;
        this.group = group;
    }

    public static ChestLocation deserialize(Map<String, Object> args) {
        String world = (String) args.get("world");
        double x = (Double) args.get("x");
        double y = (Double) args.get("y");
        double z = (Double) args.get("z");
        Object dir = args.get("dir");
        BlockFace face = null;
        if (dir instanceof Integer) {
            Direction d = Direction.fromOldInt((Integer) dir);
            face = d.toBlockFace();
        } else if (dir instanceof BlockFaceWrapper) {
            BlockFaceWrapper direction = (BlockFaceWrapper) args.get("dir");
            face = direction.face;
        }
        RandomizationGroup group = RandomizationGroup.getGroup((String) args.get("group"));

        return new ChestLocation(world, x, y, z, face, group);
    }

    public BlockFace getDir() {
        return direction;
    }

    public RandomizationGroup getGroup() {
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
        Map<String, Object> result = new HashMap<>();

        result.put("world", getWorld().getName());
        result.put("x", getX());
        result.put("y", getY());
        result.put("z", getZ());
        result.put("dir", new BlockFaceWrapper(direction));
        result.put("group", group.getName());
        return result;
    }

    public boolean similar(Location location) {
        return location.getBlockX() == this.getBlockX() && location.getBlockY() == this.getBlockY() && location.getBlockZ() == this.getBlockZ();
    }
}