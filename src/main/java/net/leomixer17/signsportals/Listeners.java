package net.leomixer17.signsportals;

import net.leomixer17.pluginlib.util.Players;
import net.leomixer17.signsportals.database.DatabaseManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class Listeners implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignChange(final SignChangeEvent e)
    {
        if (!e.getLine(0).equals(SignsPortals.getPlugin().getConfig().getString("portal_identifier")))
            return;
        if (!e.getPlayer().hasPermission("signsportals.create"))
        {
            e.getPlayer().sendMessage(Messages.getMsg("no_permission_create"));
            return;
        }
        if (SignsPortals.getPlugin().getConfig().getString("world_list_type").equalsIgnoreCase("blacklist"))
        {
            if (SignsPortals.getPlugin().getConfig().getStringList("world_list").contains(e.getBlock().getWorld().getName()))
            {
                e.getPlayer().sendMessage(Messages.getMsg("world_not_allowed"));
                return;
            }
        }
        else if (!SignsPortals.getPlugin().getConfig().getStringList("world_list").contains(e.getBlock().getWorld().getName()))
        {
            e.getPlayer().sendMessage(Messages.getMsg("world_not_allowed"));
            return;
        }
        if (SignsPortals.getEconomy().getBalance(e.getPlayer()) < SignsPortals.getPlugin().getConfig().getInt("portal_cost"))
        {
            e.getPlayer().sendMessage(Messages.getMsg("insufficient_funds"));
            return;
        }
        if (isEmptyOrWhitespaceOnly(e.getLine(1)) || isEmptyOrWhitespaceOnly(e.getLine(2)))
        {
            e.getPlayer().sendMessage(Messages.getMsg("missing_lines"));
            return;
        }
        if (e.getLine(1).equals(e.getLine(2)))
        {
            e.getPlayer().sendMessage(Messages.getMsg("portal_and_destination_equal"));
            return;
        }
        if (SignsPortals.getPortal(e.getPlayer(), e.getLine(1)) != null)
        {
            e.getPlayer().sendMessage(Messages.getMsg("portal_already_exists").replace("%portal%", e.getLine(1)));
            return;
        }
        final SignPortal portal = new SignPortal(e.getBlock(), e.getPlayer(), e.getLine(1), e.getLine(2));

        final String line1 = ChatColor.translateAlternateColorCodes('&', SignsPortals.getPlugin().getConfig().getString("sign_lines.1"))
                .replace("%player_name%", portal.getOwner().getName()).replace("%portal%", portal.getName()).replace("%destination%", portal.getDestination());
        final String line2 = ChatColor.translateAlternateColorCodes('&', SignsPortals.getPlugin().getConfig().getString("sign_lines.2"))
                .replace("%player_name%", portal.getOwner().getName()).replace("%portal%", portal.getName()).replace("%destination%", portal.getDestination());
        final String line3 = ChatColor.translateAlternateColorCodes('&', SignsPortals.getPlugin().getConfig().getString("sign_lines.3"))
                .replace("%player_name%", portal.getOwner().getName()).replace("%portal%", portal.getName()).replace("%destination%", portal.getDestination());
        final String line4 = ChatColor.translateAlternateColorCodes('&', SignsPortals.getPlugin().getConfig().getString("sign_lines.4"))
                .replace("%player_name%", portal.getOwner().getName()).replace("%portal%", portal.getName()).replace("%destination%", portal.getDestination());

        e.setLine(0, line1);
        e.setLine(1, line2);
        e.setLine(2, line3);
        e.setLine(3, line4);

        portal.save();
        final double cost = SignsPortals.getPlugin().getConfig().getDouble("portal_cost");
        SignsPortals.getEconomy().withdrawPlayer(e.getPlayer(), cost);
        e.getPlayer().sendMessage(Messages.getMsg("portal_created").replace("%money%", SignsPortals.getEconomy().format(cost)));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent e)
    {
        if (!SignsPortals.isPortal(e.getBlock()))
            return;
        final SignPortal portal = SignsPortals.getPortal(e.getBlock());
        portal.delete();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPhysics(final BlockPhysicsEvent e)
    {
        if (!SignsPortals.isPortal(e.getBlock()))
            return;
        Bukkit.getScheduler().runTask(SignsPortals.getPlugin(), new Runnable() {
            @Override
            public void run()
            {
                if (!(e.getBlock().getState() instanceof Sign))
                {
                    final SignPortal portal = SignsPortals.getPortal(e.getBlock());
                    portal.delete();
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(final PlayerJoinEvent event)
    {
        final ResultSet rs = SignsPortals.getDatabaseManager().query(DatabaseManager.GET_PLAYER_USERNAME_FROM_UUID, event.getPlayer().getUniqueId().toString());
        try
        {
            if (rs.next())
                if (!event.getPlayer().getName().equals(rs.getString(1)))
                    SignsPortals.getDatabaseManager().update(DatabaseManager.UPDATE_PLAYER_USERNAME, event.getPlayer().getName(), SignsPortals.getPlayerId(event.getPlayer().getUniqueId()));
            rs.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        SignsPortals.getPortals(event.getPlayer()).forEach(portal -> portal.update());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignInteract(final PlayerInteractEvent e)
    {
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            return;
        if (!SignsPortals.isPortal(e.getClickedBlock()))
            return;
        final SignPortal portal = SignsPortals.getPortal(e.getClickedBlock());
        if (portal.getOwner().equals(e.getPlayer()))
        {
            if (!e.getPlayer().hasPermission("signsportals.use"))
            {
                e.getPlayer().sendMessage(Messages.getMsg("no_permission_use"));
                return;
            }
        }
        else
        {
            if (!e.getPlayer().hasPermission("signsportals.use.other"))
            {
                e.getPlayer().sendMessage(Messages.getMsg("no_permission_use_other"));
                return;
            }
        }
        final SignPortal destPortal = SignsPortals.getPortal(portal.getOwner(), portal.getDestination());
        if (destPortal == null)
        {
            e.getPlayer().sendMessage(Messages.getMsg("destination_not_exists"));
            return;
        }
        final Location location = destPortal.getBlock().getLocation();
        location.setYaw(e.getPlayer().getLocation().getYaw());
        location.setPitch(e.getPlayer().getLocation().getPitch());
        e.getPlayer().teleport(SPUtils.getRoundedLocation(location));
        Players.sendActionBar(Messages.getMsg("teleport_success").replace("%portal%", portal.getName()).replace("%destination%", portal.getDestination()), e.getPlayer());
    }

    private static boolean isEmptyOrWhitespaceOnly(final String s)
    {
        return s.replace(" ", "").isEmpty();
    }
}
