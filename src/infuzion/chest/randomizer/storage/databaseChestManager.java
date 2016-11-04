package infuzion.chest.randomizer.storage;

import infuzion.chest.randomizer.ChestRandomizer;
import infuzion.chest.randomizer.util.Direction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("SqlResolve")
public class databaseChestManager extends chestManager {

    private final List<Location> toDelete = Collections.synchronizedList(new LinkedList<Location>());
    private final List<chestLocation> toCreate = Collections.synchronizedList(new LinkedList<chestLocation>());
    private Connection connection;
    private BukkitRunnable save;
    private String fullTableName;
    private boolean saveScheduled = false;

    public databaseChestManager(ChestRandomizer chestRandomizer, Connection connection, String database, String tableName) {
        super(chestRandomizer);
        this.connection = connection;
        fullTableName = database + "." + tableName;

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
                    Direction dir = Direction.fromInt(results.getInt("direction"));
                    World w = Bukkit.getWorld(world);
                    if (w == null) {
                        w = Bukkit.getWorlds().get(0);
                    }
                    Location loc = new Location(w, x, y, z);

                    chests.add(new chestLocation(loc, dir, group));
                }
                results.close();
            } else {
                String createPrimaryIndex = "CREATE UNIQUE INDEX table_name_index_uindex ON " + fullTableName + "(`index`);";
                String toExec = "CREATE TABLE " + fullTableName + "( " +
                        "`index` INT PRIMARY KEY NOT NULL AUTO_INCREMENT," +
                        "x_location INT, " +
                        "y_location INT, " +
                        "z_location INT, " +
                        "direction INT, " +
                        "`group` VARCHAR(127), " +
                        "world VARCHAR(127))";
                plugin.getLogger().severe(toExec);
                connection.prepareStatement(toExec).executeUpdate();
                connection.prepareStatement(createPrimaryIndex).executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addChest(Location location, Direction direction, String group) {
        super.addChest(location, direction, group);
        toDelete.add(location);
        toCreate.add(new chestLocation(location, direction, group));
        saveToDataBase();
    }

    @Override
    public void removeChest(final Location location) {
        toDelete.add(location);
        super.removeChest(location);
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

    void save() {
        saveToDataBase();
    }

    private void saveToDataBase() {
        saveToDataBase(false);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void saveToDataBase(boolean force) {
        if (force) {
            try {
                long b = System.currentTimeMillis();
                System.out.println(b);
                connection.setAutoCommit(false);
                synchronized (toDelete) {
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
                }
                synchronized (toCreate) {
                    PreparedStatement pS = connection.prepareStatement("" +
                            "INSERT INTO " + fullTableName +
                            "(x_location, y_location, z_location, direction, `group`, world)" +
                            "VALUES(?, ?, ?, ?, ?, ?) ");
                    for (chestLocation chest : toCreate) {
                        pS.setInt(1, chest.getBlockX());
                        pS.setInt(2, chest.getBlockY());
                        pS.setInt(3, chest.getBlockZ());
                        pS.setInt(4, chest.getDir());
                        pS.setString(5, chest.getGroup());
                        pS.setString(6, chest.getWorld().getName());
                        pS.addBatch();
                    }
                    pS.executeBatch();
                    toCreate.clear();
                }
                synchronized (chests) {
                    PreparedStatement saveStatement = connection.prepareStatement("" +
                            "UPDATE " + fullTableName + " SET " +
                            "`group` = ?, direction = ? WHERE " +
                            "x_location = ? AND y_location = ? AND z_location = ? AND world = ?");
                    for (chestLocation chest : chests) {
                        saveStatement.setString(1, chest.getGroup());
                        saveStatement.setInt(2, chest.getDir());
                        saveStatement.setInt(3, chest.getBlockX());
                        saveStatement.setInt(4, chest.getBlockY());
                        saveStatement.setInt(5, chest.getBlockZ());
                        saveStatement.setString(6, chest.getWorld().getName());
                        saveStatement.addBatch();
                    }
                    saveStatement.executeBatch();
                }
                long a = System.currentTimeMillis();
                connection.commit();
                long c = System.currentTimeMillis();
                connection.setAutoCommit(true);
                System.out.println(a);
                System.out.println("Time Total: " + String.valueOf(a - b));
                System.out.println("Time 2: " + String.valueOf(c - a));
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
                    saveToDataBase(true);
                    saveScheduled = false;
                }
            };
            save.runTaskAsynchronously(plugin);
        }
    }
}
