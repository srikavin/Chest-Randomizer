package infuzion.chest.randomizer.util;

import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class BlockFaceWrapper implements ConfigurationSerializable {
    public final BlockFace face;

    public BlockFaceWrapper(BlockFace face) {
        this.face = face;
    }

    public BlockFaceWrapper(String name) {
        this.face = BlockFace.valueOf(name);
    }

    @SuppressWarnings("unused")
    public BlockFaceWrapper(Map<String, Object> map) {
        this.face = BlockFace.valueOf((String) map.get("direction"));
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("direction", face.toString());
        return map;
    }
}
