package infuzion.chest.randomizer.storage.impl;

import infuzion.chest.randomizer.ChestRandomizer;
import infuzion.chest.randomizer.storage.ChestLocation;
import infuzion.chest.randomizer.storage.ChestManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("SqlResolve")
public class DatabaseChestManager extends ChestManager {

    private final List<Location> toDelete = new CopyOnWriteArrayList<>();
    private final List<ChestLocation> toCreate = new CopyOnWriteArrayList<>();
    private Connection connection;
    private BukkitRunnable save;
    private String fullTableName;
    private boolean saveScheduled = false;

    public DatabaseChestManager(ChestRandomizer chestRandomizer, Connection connection, String database, String tableName) {
        super(chestRandomizer);
        this.connection = connection;
        fullTableName = database + '.' + tableName;

        try {
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, tableName, null);
            if (tables.next()) {
                String query = "SELECT * FROM " + fullTableName;
                ResultSet results = connection.createStatement().executeQuery(query);
                while (results.next()) {
                    int x = results.getInt("x_location");
                    int y = results.getInt("y_location");
                    int z = results.getInt("z_location");
                    String group = results.getString("group");
                    String world = results.getString("world");
                    BlockFace dir = BlockFace.valueOf(results.getString("direction"));
                    World w = Bukkit.getWorld(world);
                    if (w == null) {
                        w = Bukkit.getWorlds().get(0);
                    }
                    Location loc = new Location(w, x, y, z);

                    chests.add(new ChestLocation(loc, dir, group));
                }
                results.close();
            } else {
                String createPrimaryIndex = "CREATE UNIQUE INDEX table_name_index_uindex ON " + fullTableName + "(`index`);";
                String toExec = "CREATE TABLE " + fullTableName + "( " +
                        "`index` INT PRIMARY KEY NOT NULL AUTO_INCREMENT," +
                        "x_location INT, " +
                        "y_location INT, " +
                        "z_location INT, " +
                        "direction VARCHAR(16), " +
                        "`group` VARCHAR(127), " +
                        "world VARCHAR(127))";
                plugin.getLevelLogger().severe(toExec);
                connection.prepareStatement(toExec).executeUpdate();
                connection.prepareStatement(createPrimaryIndex).executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addChest(Location location, BlockFace direction, String group) {
        super.addChest(location, direction, group);
        toDelete.add(location);
        toCreate.add(new ChestLocation(location, direction, group));
        saveToDataBase();
    }

    @Override
    public void removeChest(final Location location) {
        super.removeChest(location);
        toDelete.add(location);
        saveToDataBase();
    }

    public void cleanUp() {
        try {
            if (save != null) {
                save.cancel();
            }
            saveToDataBase(true);
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void save() {
        saveToDataBase();
    }

    private void saveToDataBase() {
        saveToDataBase(false);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void saveToDataBase(boolean force) {
        if (force) {
            try {
                PreparedStatement pS = connection.prepareStatement("DELETE FROM " + fullTableName +
                        " WHERE x_location = ? AND y_location = ? AND z_location = ?");
                for (Location chest : toDelete) {
                    pS.setInt(1, chest.getBlockX());
                    pS.setInt(2, chest.getBlockY());
                    pS.setInt(3, chest.getBlockZ());
                    pS.addBatch();
                }
                pS.executeBatch();
                toDelete.clear();
                pS = connection.prepareStatement(
                        "INSERT INTO " + fullTableName +
                                "(x_location, y_location, z_location, direction, `group`, world)"
                                + "VALUES(?, ?, ?, ?, ?, ?) ");
                for (ChestLocation chest : toCreate) {
                    pS.setInt(1, chest.getBlockX());
                    pS.setInt(2, chest.getBlockY());
                    pS.setInt(3, chest.getBlockZ());
                    pS.setString(4, chest.getDir().toString());
                    pS.setString(5, chest.getGroup().getName());
                    pS.setString(6, chest.getWorld().getName());
                    pS.addBatch();
                }
                pS.executeBatch();
                toCreate.clear();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (saveScheduled) {
            //noinspection UnnecessaryReturnStatement
            return;
        } else {
            saveScheduled = true;
            save = new BukkitRunnable() {
                public void run() {
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    saveToDataBase(true);
                    saveScheduled = false;
                }
            };
            save.runTaskAsynchronously(plugin);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        connection.close();
        super.finalize();
    }
}
