package net.leonardo_dgs.signsportals;

import co.aikar.idb.DB;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class SignPortal {

    private Block block;
    private String name;
    private String destination;
    private OfflinePlayer owner;

    public SignPortal(Block block, OfflinePlayer owner, String name, String destination)
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

    public void setBlock(Block block)
    {
        this.block = block;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDestination()
    {
        return this.destination;
    }

    public void setDestination(String destination)
    {
        this.destination = destination;
    }

    public OfflinePlayer getOwner()
    {
        return owner;
    }

    public void setOwner(OfflinePlayer owner)
    {
        this.owner = owner;
    }

    @SneakyThrows
    public void save()
    {
        DB.executeUpdate(DatabaseQueries.INSERT_PORTAL,
                SPUtils.serializeLocation(this.getBlock().getLocation()),
                SignsPortals.getPlayerId(this.getOwner().getUniqueId()),
                this.getName(), this.getDestination());
    }

    public void update()
    {
        String line1 = ChatColor.translateAlternateColorCodes('&', SignsPortals.getInstance().getConfig().getString("sign_lines.1"))
                .replace("%player_name%", this.getOwner().getName()).replace("%portal%", this.getName()).replace("%destination%", this.getDestination());
        String line2 = ChatColor.translateAlternateColorCodes('&', SignsPortals.getInstance().getConfig().getString("sign_lines.2"))
                .replace("%player_name%", this.getOwner().getName()).replace("%portal%", this.getName()).replace("%destination%", this.getDestination());
        String line3 = ChatColor.translateAlternateColorCodes('&', SignsPortals.getInstance().getConfig().getString("sign_lines.3"))
                .replace("%player_name%", this.getOwner().getName()).replace("%portal%", this.getName()).replace("%destination%", this.getDestination());
        String line4 = ChatColor.translateAlternateColorCodes('&', SignsPortals.getInstance().getConfig().getString("sign_lines.4"))
                .replace("%player_name%", this.getOwner().getName()).replace("%portal%", this.getName()).replace("%destination%", this.getDestination());
        Sign sign = (Sign) this.getBlock().getState();
        sign.setLine(0, line1);
        sign.setLine(1, line2);
        sign.setLine(2, line3);
        sign.setLine(3, line4);
        sign.update();
    }

    @SneakyThrows
    public void delete()
    {
        DB.executeUpdate(DatabaseQueries.DELETE_PORTAL, SPUtils.serializeLocation(this.getBlock().getLocation()));
        if (this.getOwner() != null)
        {
            double refund = SignsPortals.getInstance().getConfig().getDouble("portal_refund");
            SignsPortals.getEconomy().depositPlayer(this.getOwner(), refund);
            if (this.getOwner().isOnline())
                this.getOwner().getPlayer().sendMessage(Messages.getMsg("portal_broken").replace("%money%", SignsPortals.getEconomy().format(refund))
                        .replace("%portal%", this.getName()).replace("%destination%", this.getDestination())
                );
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof SignPortal))
            return false;
        return this.getBlock().getLocation().equals(((SignPortal) obj).getBlock().getLocation());
    }

}
