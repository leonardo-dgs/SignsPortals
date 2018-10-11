package net.leomixer17.signsportals;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public final class Messages {

    private static FileConfiguration messages;

    static String getMsg(final String key)
    {
        return (!key.equals("prefix") ? getMsg("prefix") : "") + ChatColor.translateAlternateColorCodes('&', messages.getString(key));
    }

    static void loadMessages()
    {
        final File f = new File(SignsPortals.getPlugin().getDataFolder(), "messages.yml");
        if (!f.exists())
            try
            {
                f.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        messages = Config.loadDefaults(YamlConfiguration.loadConfiguration(f), YamlConfiguration.loadConfiguration(new InputStreamReader(SignsPortals.getPlugin().getResource("messages.yml"))));
        try
        {
            messages.save(f);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
