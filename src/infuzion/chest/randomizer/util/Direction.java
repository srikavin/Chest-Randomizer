package infuzion.chest.randomizer.util;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public enum Direction implements ConfigurationSerializable {
    NORTH(2),
    SOUTH(3),
    EAST(5),
    WEST(4);

    private static HashMap<Integer, Direction> map = new HashMap<Integer, Direction>();

    static {
        for (Direction e : Direction.values()) {
            map.put(e.dir, e);
        }
    }

    private int dir = -1;

    Direction(final int dir) {
        this.dir = dir;
    }

    public static Direction valueOf(int dir) {
        return map.get(dir);
    }

    public int getDirection() {
        return dir;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("Direction", dir);
        return ret;
    }
}
