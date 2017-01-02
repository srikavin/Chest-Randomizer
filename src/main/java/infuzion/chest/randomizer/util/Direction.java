package infuzion.chest.randomizer.util;

import org.bukkit.block.BlockFace;

import java.util.HashMap;
import java.util.Map;

public enum Direction {
    NORTH(3, 2),
    SOUTH(1, 3),
    EAST(4, 5),
    WEST(2, 4);

    private static Map<Integer, Direction> oldValues = new HashMap<>();
    private static Map<Integer, Direction> values = new HashMap<>();

    static {
        for (Direction e : Direction.values()) {
            values.put(e.dir, e);
            oldValues.put(e.oldDir, e);
        }
    }

    private int dir = -1;
    private int oldDir = -1;

    Direction(final int dir, final int oldDir) {
        this.dir = dir;
        this.oldDir = oldDir;
    }

    public static Direction fromInt(int dir) {
        return values.get(dir);
    }

    public static Direction fromOldInt(int dir) {
        return oldValues.get(dir);
    }

    public BlockFace toBlockFace() {
        if (this == NORTH) {
            return BlockFace.NORTH;
        }
        if (this == SOUTH) {
            return BlockFace.SOUTH;
        }
        if (this == EAST) {
            return BlockFace.EAST;
        }
        if (this == WEST) {
            return BlockFace.WEST;
        }
        return null;
    }

    public int getOldDir() {
        return oldDir;
    }
}
