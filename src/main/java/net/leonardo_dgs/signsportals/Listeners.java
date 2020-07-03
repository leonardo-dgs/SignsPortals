package net.leonardo_dgs.signsportals;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import net.leonardo_dgs.signsportals.util.PlayerUtil;
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

import java.sql.SQLException;

public final class Listeners implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        if (!event.getLine(0).equals(SignsPortals.getInstance().getConfig().getString("portal_identifier")))
            return;
        if (!event.getPlayer().hasPermission("signsportals.create")) {
            event.getPlayer().sendMessage(Messages.getMsg("no_permission_create"));
            return;
        }
        if (SignsPortals.getInstance().getConfig().getString("world_list_type").equalsIgnoreCase("blacklist")) {
            if (SignsPortals.getInstance().getConfig().getStringList("world_list").contains(event.getBlock().getWorld().getName())) {
                event.getPlayer().sendMessage(Messages.getMsg("world_not_allowed"));
                return;
            }
        } else if (!SignsPortals.getInstance().getConfig().getStringList("world_list").contains(event.getBlock().getWorld().getName())) {
            event.getPlayer().sendMessage(Messages.getMsg("world_not_allowed"));
            return;
        }
        if (SignsPortals.getEconomy().getBalance(event.getPlayer()) < SignsPortals.getInstance().getConfig().getInt("portal_cost")) {
            event.getPlayer().sendMessage(Messages.getMsg("insufficient_funds"));
            return;
        }
        if (isEmptyOrWhitespaceOnly(event.getLine(1)) || isEmptyOrWhitespaceOnly(event.getLine(2))) {
            event.getPlayer().sendMessage(Messages.getMsg("missing_lines"));
            return;
        }
        if (event.getLine(1).equals(event.getLine(2))) {
            event.getPlayer().sendMessage(Messages.getMsg("portal_and_destination_equal"));
            return;
        }
        if (SignsPortals.getPortal(event.getPlayer(), event.getLine(1)) != null) {
            event.getPlayer().sendMessage(Messages.getMsg("portal_already_exists").replace("%portal%", event.getLine(1)));
            return;
        }
        SignPortal portal = new SignPortal(event.getBlock(), event.getPlayer(), event.getLine(1), event.getLine(2));

        String line1 = ChatColor.translateAlternateColorCodes('&', SignsPortals.getInstance().getConfig().getString("sign_lines.1"))
                .replace("%player_name%", portal.getOwner().getName()).replace("%portal%", portal.getName()).replace("%destination%", portal.getDestination());
        String line2 = ChatColor.translateAlternateColorCodes('&', SignsPortals.getInstance().getConfig().getString("sign_lines.2"))
                .replace("%player_name%", portal.getOwner().getName()).replace("%portal%", portal.getName()).replace("%destination%", portal.getDestination());
        String line3 = ChatColor.translateAlternateColorCodes('&', SignsPortals.getInstance().getConfig().getString("sign_lines.3"))
                .replace("%player_name%", portal.getOwner().getName()).replace("%portal%", portal.getName()).replace("%destination%", portal.getDestination());
        String line4 = ChatColor.translateAlternateColorCodes('&', SignsPortals.getInstance().getConfig().getString("sign_lines.4"))
                .replace("%player_name%", portal.getOwner().getName()).replace("%portal%", portal.getName()).replace("%destination%", portal.getDestination());

        event.setLine(0, line1);
        event.setLine(1, line2);
        event.setLine(2, line3);
        event.setLine(3, line4);

        portal.save();
        double cost = SignsPortals.getInstance().getConfig().getDouble("portal_cost");
        SignsPortals.getEconomy().withdrawPlayer(event.getPlayer(), cost);
        event.getPlayer().sendMessage(Messages.getMsg("portal_created").replace("%money%", SignsPortals.getEconomy().format(cost)));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (!SignsPortals.isPortal(e.getBlock()))
            return;
        SignPortal portal = SignsPortals.getPortal(e.getBlock());
        portal.delete();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (!SignsPortals.isPortal(event.getBlock()))
            return;
        Bukkit.getScheduler().runTask(SignsPortals.getInstance(), () ->
        {
            if (!(event.getBlock().getState() instanceof Sign)) {
                SignPortal portal = SignsPortals.getPortal(event.getBlock());
                portal.delete();
            }
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            DbRow result = DB.getFirstRow(DatabaseQueries.GET_PLAYER_USERNAME_FROM_UUID, event.getPlayer().getUniqueId().toString());
            if (result != null)
                if (!event.getPlayer().getName().equals(result.getString("username")))
                    DB.executeUpdate(DatabaseQueries.UPDATE_PLAYER_USERNAME, event.getPlayer().getName(), SignsPortals.getPlayerId(event.getPlayer().getUniqueId()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        SignsPortals.getPortals(event.getPlayer()).forEach(portal -> portal.update());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignInteract(PlayerInteractEvent e) {
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            return;
        if (!SignsPortals.isPortal(e.getClickedBlock()))
            return;
        SignPortal portal = SignsPortals.getPortal(e.getClickedBlock());
        if (portal.getOwner().equals(e.getPlayer())) {
            if (!e.getPlayer().hasPermission("signsportals.use")) {
                e.getPlayer().sendMessage(Messages.getMsg("no_permission_use"));
                return;
            }
        } else {
            if (!e.getPlayer().hasPermission("signsportals.use.other")) {
                e.getPlayer().sendMessage(Messages.getMsg("no_permission_use_other"));
                return;
            }
        }
        SignPortal destPortal = SignsPortals.getPortal(portal.getOwner(), portal.getDestination());
        if (destPortal == null) {
            e.getPlayer().sendMessage(Messages.getMsg("destination_not_exists"));
            return;
        }
        Location location = destPortal.getBlock().getLocation();
        location.setYaw(e.getPlayer().getLocation().getYaw());
        location.setPitch(e.getPlayer().getLocation().getPitch());
        e.getPlayer().teleport(SPUtils.getRoundedLocation(location));
        PlayerUtil.sendActionBar(Messages.getMsg("teleport_success").replace("%portal%", portal.getName()).replace("%destination%", portal.getDestination()), e.getPlayer());
    }

    private static boolean isEmptyOrWhitespaceOnly(String s) {
        return s.replace(" ", "").isEmpty();
    }
}
