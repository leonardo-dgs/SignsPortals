package net.leonardo_dgs.signsportals.database;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public final class DatabaseConnectionSettings {

    private String host;
    private int port;
    private String database;
    private String user;
    private String password;
    private Map<String, String> properties;

    public DatabaseConnectionSettings()
    {
        this("localhost", 3306, "Minecraft", "root", "", new HashMap<String, String>());
    }

    public DatabaseConnectionSettings(final String host, final int port, final String database, final String user, final String password)
    {
        this(host, port, database, user, password, new HashMap<String, String>());
    }

    public DatabaseConnectionSettings(final String host, final int port, final String database, final String user, final String password, final Map<String, String> properties)
    {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
        this.properties = properties;
    }

    public String getPropertiesString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("?");
        this.getProperties().keySet().forEach(key -> sb.append(key + "=" + this.getProperties().get(key) + "&"));
        return sb.substring(0, sb.length() - 1);
    }

}