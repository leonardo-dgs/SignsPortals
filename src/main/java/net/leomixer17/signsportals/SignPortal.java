package net.leomixer17.signsportals;

import net.leomixer17.signsportals.database.DatabaseManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class SignPortal {

    private Block block;
    private String name;
    private String destination;
    private OfflinePlayer owner;

    public SignPortal(final Block block, final OfflinePlayer owner, final String name, final String destination)
    {
        this.setBlock(block);
        this.setOwner(owner);
        this.setName(name);
        this.setDestination(destination);
    }

    public Block getBlock()
    {
        return this.block;
    }

    public void setBlock(final Block block)
    {
        this.block = block;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getDestination()
    {
        return this.destination;
    }

    public void setDestination(final String destination)
    {
        this.destination = destination;
    }

    public OfflinePlayer getOwner()
    {
        return owner;
    }

    public void setOwner(final OfflinePlayer owner)
    {
        this.owner = owner;
    }

    public void save()
    {
        SignsPortals.getDatabaseManager().update(DatabaseManager.INSERT_PORTAL, SPUtils.serializeLocation(this.getBlock().getLocation()),
                SignsPortals.getPlayerId(this.getOwner().getUniqueId()), this.getName(), this.getDestination());
    }

    public void update()
    {
        final String line1 = ChatColor.translateAlternateColorCodes('&', SignsPortals.getPlugin().getConfig().getString("sign_lines.1"))
                .replace("%player_name%", this.getOwner().getName()).replace("%portal%", this.getName()).replace("%destination%", this.getDestination());
        final String line2 = ChatColor.translateAlternateColorCodes('&', SignsPortals.getPlugin().getConfig().getString("sign_lines.2"))
                .replace("%player_name%", this.getOwner().getName()).replace("%portal%", this.getName()).replace("%destination%", this.getDestination());
        final String line3 = ChatColor.translateAlternateColorCodes('&', SignsPortals.getPlugin().getConfig().getString("sign_lines.3"))
                .replace("%player_name%", this.getOwner().getName()).replace("%portal%", this.getName()).replace("%destination%", this.getDestination());
        final String line4 = ChatColor.translateAlternateColorCodes('&', SignsPortals.getPlugin().getConfig().getString("sign_lines.4"))
                .replace("%player_name%", this.getOwner().getName()).replace("%portal%", this.getName()).replace("%destination%", this.getDestination());
        final Sign sign = (Sign) this.getBlock().getState();
        sign.setLine(0, line1);
        sign.setLine(1, line2);
        sign.setLine(2, line3);
        sign.setLine(3, line4);
        sign.update();
    }

    public void delete()
    {
        SignsPortals.getDatabaseManager().update(DatabaseManager.DELETE_PORTAL, SPUtils.serializeLocation(this.getBlock().getLocation()));
        if (this.getOwner() != null)
        {
            final double refund = SignsPortals.getPlugin().getConfig().getDouble("portal_refund");
            SignsPortals.getEconomy().depositPlayer(this.getOwner(), refund);
            if (this.getOwner().isOnline())
                this.getOwner().getPlayer().sendMessage(Messages.getMsg("portal_broken").replace("%money%", SignsPortals.getEconomy().format(refund))
                        .replace("%portal%", this.getName()).replace("%destination%", this.getDestination())
                );
        }
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (!(obj instanceof SignPortal))
            return false;
        return this.getBlock().getLocation().equals(((SignPortal) obj).getBlock().getLocation());
    }

}
