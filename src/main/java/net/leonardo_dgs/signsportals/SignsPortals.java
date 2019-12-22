package net.leonardo_dgs.signsportals;

import lombok.Getter;
import net.leonardo_dgs.signsportals.database.DatabaseConnectionSettings;
import net.leonardo_dgs.signsportals.database.DatabaseManager;
import net.leonardo_dgs.signsportals.database.DatabaseType;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public final class SignsPortals extends JavaPlugin {

    @Getter
    private static SignsPortals instance;
    @Getter
    private static DatabaseManager databaseManager;
    @Getter
    private static Economy economy;

    @Override
    public void onEnable()
    {
        instance = this;
        if (!this.setupEconomy())
        {
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
    public void onDisable()
    {
        databaseManager.disconnect();
    }

    private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null)
            economy = economyProvider.getProvider();
        return (economy != null);
    }

    private void setupDatabase()
    {
        DatabaseType type;
        try
        {
            type = DatabaseType.valueOf(this.getConfig().getString("database.backend").toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            type = DatabaseType.SQLITE;
        }
        final DatabaseConnectionSettings settings = new DatabaseConnectionSettings();
        final Map<String, String> properties = new HashMap<>();
        String databaseName = this.getConfig().getString("database.database_name");
        if (type.equals(DatabaseType.MYSQL))
        {
            settings.setHost(this.getConfig().getString("database.mysql.host"));
            settings.setPort(this.getConfig().getInt("database.mysql.port"));
            settings.setUser(this.getConfig().getString("database.mysql.user"));
            settings.setPassword(this.getConfig().getString("database.mysql.password"));
            properties.put("useSSL", String.valueOf(this.getConfig().getBoolean("database.mysql.useSSL")));
        }
        else
        {
            databaseName = this.getDataFolder().getAbsolutePath() + File.separator + databaseName + ".db";
        }
        settings.setDatabase(databaseName);
        settings.setProperties(properties);
        databaseManager = new DatabaseManager(type, settings);
        databaseManager.initialise();
    }

    public static SignPortal getPortal(Block block)
    {
        final ResultSet rs = getDatabaseManager().query(DatabaseManager.GET_PORTAL_FROM_BLOCK, SPUtils.serializeLocation(block.getLocation()));
        try
        {
            if (rs.next())
            {
                final OfflinePlayer owner = getPlayer(rs.getInt(1));
                final String name = rs.getString(2);
                final String destName = rs.getString(3);
                return new SignPortal(block, owner, name, destName);
            }
            rs.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static SignPortal getPortal(OfflinePlayer owner, String name)
    {
        final ResultSet rs = getDatabaseManager().query(DatabaseManager.GET_PORTAL_FROM_OWNER_AND_NAME, getPlayerId(owner), name);
        try
        {
            if (rs.next())
            {
                final Location location = SPUtils.deserializeLocation(rs.getString(1));
                final String destName = rs.getString(2);
                final SignPortal portal = new SignPortal(location.getBlock(), owner, name, destName);
                if (location.getBlock().getState() instanceof Sign)
                    return portal;
                portal.delete();
            }
            rs.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static Set<SignPortal> getPortals(OfflinePlayer player)
    {
        final Set<SignPortal> portals = new HashSet<>();
        final ResultSet rs = getDatabaseManager().query(DatabaseManager.GET_PLAYER_PORTALS, getPlayerId(player));
        try
        {
            while (rs.next())
            {
                final Location location = SPUtils.deserializeLocation(rs.getString(1));
                final String name = rs.getString(2);
                final String destName = rs.getString(3);
                final SignPortal portal = new SignPortal(location.getBlock(), player, name, destName);
                if (location.getBlock().getState() instanceof Sign)
                    portals.add(portal);
                else
                    portal.delete();
            }
            rs.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return portals;
    }

    public static boolean isPortal(Block block)
    {
        if (!(block.getState() instanceof Sign))
            return false;
        return getPortal(block) != null;
    }

    public static int getWorldId(UUID uid)
    {
        int id = 0;
        final ResultSet rs = getDatabaseManager().query(DatabaseManager.GET_WORLD_ID_FROM_UID, uid.toString());
        try
        {
            if (rs.next())
                id = rs.getInt(1);
            else
            {
                SignsPortals.getDatabaseManager().update(DatabaseManager.INSERT_WORLD, uid.toString());
                id = SignsPortals.getWorldId(uid);
            }
            rs.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return id;
    }

    public static int getPlayerId(OfflinePlayer player)
    {
        return getPlayerId(player.getUniqueId());
    }

    public static int getPlayerId(UUID uuid)
    {
        int id = 0;
        final ResultSet rs = getDatabaseManager().query(DatabaseManager.GET_PLAYER_ID_FROM_UUID, uuid.toString());
        try
        {
            if (rs.next())
                id = rs.getInt(1);
            else
            {
                SignsPortals.getDatabaseManager().update(DatabaseManager.INSERT_PLAYER, Bukkit.getOfflinePlayer(uuid).getName(), uuid.toString());
                id = SignsPortals.getPlayerId(uuid);
            }
            rs.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return id;
    }

    public static int getPlayerId(String username)
    {
        int id = 0;
        final ResultSet rs = getDatabaseManager().query(DatabaseManager.GET_PLAYER_ID_FROM_USERNAME, username);
        try
        {
            if (rs.next())
                id = rs.getInt(1);
            rs.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return id;
    }

    public static World getWorld(int id)
    {
        UUID uid = null;
        final ResultSet rs = getDatabaseManager().query(DatabaseManager.GET_WORLD_UID, id);
        try
        {
            if (rs.next())
                uid = UUID.fromString(rs.getString(1));
            rs.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return uid == null ? null : Bukkit.getWorld(uid);
    }

    public static UUID getPlayerUUID(int id)
    {
        UUID uuid = null;
        final ResultSet rs = getDatabaseManager().query(DatabaseManager.GET_PLAYER_UUID, id);
        try
        {
            if (rs.next())
                uuid = UUID.fromString(rs.getString(1));
            rs.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return uuid;
    }

    public static String getPlayerName(int id)
    {
        String username = null;
        final ResultSet rs = getDatabaseManager().query(DatabaseManager.GET_PLAYER_USERNAME, id);
        try
        {
            if (rs.next())
                username = rs.getString(1);
            rs.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return username;
    }

    public static OfflinePlayer getPlayer(int id)
    {
        final UUID uuid = getPlayerUUID(id);
        return uuid == null ? null : Bukkit.getOfflinePlayer(uuid);
    }
}
