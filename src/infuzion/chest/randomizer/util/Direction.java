package infuzion.chest.randomizer.util;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public enum Direction implements ConfigurationSerializable {
    NORTH(2),
    SOUTH(3),
    EAST(5),
    WEST(4),
    UNSET(-1);

    private static Map<Integer, Direction> map = new HashMap<Integer, Direction>();

    static {
        for (Direction e : Direction.values()) {
            map.put(e.dir, e);
        }
    }

    private int dir = -1;

    Direction(final int dir) {
        this.dir = dir;
    }

    public static Direction fromString(String str) {
        switch (str.charAt(0)) {
            case 'n':
                return fromInt(2);
            case 's':
                return fromInt(3);
            case 'e':
                return fromInt(5);
            case 'w':
                return fromInt(4);
            default:
                return fromInt(-1);
        }
    }

    public static Direction fromInt(int dir) {
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
