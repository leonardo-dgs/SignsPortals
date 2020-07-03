package net.leonardo_dgs.signsportals;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

final class Config {

    static void loadAll() {
        SignsPortals.getInstance().saveDefaultConfig();
        Messages.loadMessages();
    }

    static YamlConfiguration loadDefaults(Configuration conf, Configuration defconf) {
        final YamlConfiguration finalConfig = new YamlConfiguration();
        for (final String s : defconf.getKeys(true))
            if (conf.get(s) != null)
                finalConfig.set(s, conf.get(s));
            else
                finalConfig.set(s, defconf.get(s));

        for (final String s : conf.getKeys(true))
            if (finalConfig.get(s) == null)
                finalConfig.set(s, conf.get(s));

        return finalConfig;
    }

}
