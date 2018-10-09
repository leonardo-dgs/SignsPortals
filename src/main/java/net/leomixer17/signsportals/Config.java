package net.leomixer17.signsportals;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

final class Config {
	
	static void loadAll()
	{
		SignsPortals.getPlugin().saveDefaultConfig();
		Messages.loadMessages();
	}
	
	static YamlConfiguration loadDefaults(final Configuration conf, final Configuration defconf)
	{
		final YamlConfiguration finalconfig = new YamlConfiguration();
		for (final String s : defconf.getKeys(true))
			if (conf.get(s) != null)
				finalconfig.set(s, conf.get(s));
			else
				finalconfig.set(s, defconf.get(s));
		
		for (final String s : conf.getKeys(true))
			if (finalconfig.get(s) == null)
				finalconfig.set(s, conf.get(s));
		
		return finalconfig;
	}
	
}
