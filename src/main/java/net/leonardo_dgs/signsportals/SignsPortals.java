package net.leonardo_dgs.signsportals;

import co.aikar.idb.DB;
import co.aikar.idb.Database;
import co.aikar.idb.DatabaseOptions;
import co.aikar.idb.DbRow;
import co.aikar.idb.PooledDatabaseOptions;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.MetricsLite;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public final class SignsPortals extends JavaPlugin {

    @Getter
    private static SignsPortals instance;
    @Getter
    private static Economy economy;

    @Override
    public void onEnable() {
        instance = this;
        if (!this.setupEconomy()) {
            this.getLogger().log(Level.SEVERE, "§cThis plugin requires a Vault economy plugin. Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        Config.loadAll();
        this.setupDatabase();
        Bukkit.getPluginManager().registerEvents(new Listeners(), this);
        new MetricsLite(this);
    }

    @Override
    public void onDisable() {
        DB.close();
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null)
            economy = economyProvider.getProvider();
        return (economy != null);
    }

    private void setupDatabase() {
        DatabaseOptions.DatabaseOptionsBuilder optionsBuilder = DatabaseOptions.builder().poolName(getDescription().getName() + " DB").logger(getLogger());

        String databaseType = getConfig().getString("database.backend").toUpperCase();
        Map<String, Object> properties = new HashMap<>();
        String databaseName = this.getConfig().getString("database.database_name");
        switch (databaseType) {
            default:
            case "SQLITE":
                optionsBuilder.sqlite(getDataFolder().getAbsolutePath() + File.separator + databaseName + ".db");
                break;
            case "MYSQL":
                String user = getConfig().getString("database.mysql.user");
                String password = getConfig().getString("database.mysql.password");
                String host = getConfig().getString("database.mysql.host");
                int port = getConfig().getInt("database.mysql.port");

                optionsBuilder.mysql(user, password, databaseName, host + ":" + port);
                properties.put("useSSL", getConfig().getBoolean("database.mysql.useSSL"));
                break;
        }

        Database db = PooledDatabaseOptions.builder().dataSourceProperties(properties).options(optionsBuilder.build()).createHikariDatabase();
        DB.setGlobalDatabase(db);
        DatabaseQueries.createSchema(databaseType);
    }

    public static SignPortal getPortal(Block block) {
        try {
            DbRow result = DB.getFirstRow(DatabaseQueries.GET_PORTAL_FROM_BLOCK, SPUtils.serializeLocation(block.getLocation()));
            if (result == null)
                return null;
            OfflinePlayer owner = getPlayer(result.getInt("owner"));
            String name = result.getString("name");
            String destName = result.getString("destination");
            return new SignPortal(block, owner, name, destName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SignPortal getPortal(OfflinePlayer owner, String name) {
        try {
            DbRow result = DB.getFirstRow(DatabaseQueries.GET_PORTAL_FROM_OWNER_AND_NAME, getPlayerId(owner), name);
            if (result == null)
                return null;
            Location location = SPUtils.deserializeLocation(result.getString("location"));
            String destName = result.getString("destination");
            SignPortal portal = new SignPortal(location.getBlock(), owner, name, destName);
            if (location.getBlock().getState() instanceof Sign)
                return portal;
            else
                portal.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Set<SignPortal> getPortals(OfflinePlayer player) {
        Set<SignPortal> portals = new HashSet<>();
        try {
            List<DbRow> results = DB.getResults(DatabaseQueries.GET_PLAYER_PORTALS, getPlayerId(player));
            results.forEach(result ->
            {
                Location location = SPUtils.deserializeLocation(result.getString("location"));
                String name = result.getString("name");
                String destName = result.getString("destination");
                SignPortal portal = new SignPortal(location.getBlock(), player, name, destName);
                if (location.getBlock().getState() instanceof Sign)
                    portals.add(portal);
                else
                    portal.delete();
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return portals;
    }

    public static boolean isPortal(Block block) {
        if (block.getState() instanceof Sign)
            return getPortal(block) != null;
        else
            return false;
    }

    public static int getWorldId(UUID uid) {
        int id = 0;
        try {
            DbRow result = DB.getFirstRow(DatabaseQueries.GET_WORLD_ID_FROM_UID, uid.toString());

            if (result == null) {
                DB.executeUpdate(DatabaseQueries.INSERT_WORLD, uid.toString());
                id = SignsPortals.getWorldId(uid);
            } else {
                id = result.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public static int getPlayerId(OfflinePlayer player) {
        return getPlayerId(player.getUniqueId());
    }

    public static int getPlayerId(UUID uuid) {
        int id = 0;
        try {
            DbRow result = DB.getFirstRow(DatabaseQueries.GET_PLAYER_ID_FROM_UUID, uuid.toString());
            if (result == null) {
                DB.executeUpdate(DatabaseQueries.INSERT_PLAYER, Bukkit.getOfflinePlayer(uuid).getName(), uuid.toString());
                id = SignsPortals.getPlayerId(uuid);
            } else {
                id = result.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public static int getPlayerId(String username) {
        int id = 0;
        try {
            DbRow result = DB.getFirstRow(DatabaseQueries.GET_PLAYER_ID_FROM_USERNAME, username);
            if (result != null)
                id = result.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public static World getWorld(int id) {
        UUID uid = null;
        try {
            DbRow result = DB.getFirstRow(DatabaseQueries.GET_WORLD_UID, id);
            if (result != null)
                uid = UUID.fromString(result.getString("world_uid"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return uid == null ? null : Bukkit.getWorld(uid);
    }

    public static UUID getPlayerUUID(int id) {
        UUID uuid = null;
        try {
            DbRow result = DB.getFirstRow(DatabaseQueries.GET_PLAYER_UUID, id);
            if (result != null)
                uuid = UUID.fromString(result.getString("uuid"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return uuid;
    }

    public static String getPlayerName(int id) {
        String username = null;
        try {
            DbRow result = DB.getFirstRow(DatabaseQueries.GET_PLAYER_USERNAME, id);
            if (result != null)
                username = result.getString("username");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return username;
    }

    public static OfflinePlayer getPlayer(int id) {
        final UUID uuid = getPlayerUUID(id);
        return uuid == null ? null : Bukkit.getOfflinePlayer(uuid);
    }
}
