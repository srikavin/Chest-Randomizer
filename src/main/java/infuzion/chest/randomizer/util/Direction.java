package infuzion.chest.randomizer.util;

import org.bukkit.block.BlockFace;

import java.util.HashMap;
import java.util.Map;

public enum Direction {
    NORTH(2),
    SOUTH(3),
    EAST(5),
    WEST(4);

    private static Map<Integer, Direction> oldValues = new HashMap<>();

    static {
        for (Direction e : Direction.values()) {
            oldValues.put(e.oldDir, e);
        }
    }

    private int oldDir = -1;

    Direction(final int oldDir) {
        this.oldDir = oldDir;
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

}
