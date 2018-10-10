package net.leomixer17.signsportals.database;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;

public class SQLDatabase {
    
    private DatabaseType type;
    private String url;
    private final DatabaseConnectionSettings settings;
    private Connection connection;
    
    public SQLDatabase(final DatabaseType type, final DatabaseConnectionSettings settings)
    {
        this.settings = settings;
        this.setType(type);
    }
    
    public void connect()
    {
        try
        {
            Class.forName(this.getType().getDriver());
            switch (this.getType())
            {
                case H2:
                case SQLITE:
                    final File dbFile = new File(this.getSettings().getDatabase());
                    if (!dbFile.exists())
                        dbFile.createNewFile();
                    break;
                
                default:
                    break;
            }
            
            this.connection =
                    DriverManager.getConnection(this.getConnectionUrl(), this.getSettings().getUser(), this.getSettings().getPassword());
        }
        catch (ClassNotFoundException | SQLException | IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public void disconnect()
    {
        if (this.connection != null)
            try
            {
                this.getConnection().close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
    }
    
    public Connection getConnection()
    {
        return this.connection;
    }
    
    public void setConnection(final Connection connection)
    {
        this.connection = connection;
    }
    
    public boolean isConnected() throws SQLException
    {
        return this.connection != null && this.connection.isValid(0);
    }
    
    public DatabaseConnectionSettings getSettings()
    {
        return this.settings;
    }
    
    public String getConnectionUrl()
    {
        return this.url;
    }
    
    private void refreshConnectionUrl()
    {
        this.url = this.type.getUrlFormat().replace("%host%", this.getSettings().getHost()).replace("%port%", String.valueOf(this.getSettings().getPort()))
                .replace("%database%", this.getSettings().getDatabase()).replace("%properties%", this.getSettings().getPropertiesString());
    }
    
    public DatabaseType getType()
    {
        return this.type;
    }
    
    public void setType(final DatabaseType type)
    {
        this.type = type;
        this.refreshConnectionUrl();
    }
    
    public void update(final String sql, final Object... variables)
    {
        try
        {
            final boolean autoCommit = this.getConnection().getAutoCommit();
            this.getConnection().setAutoCommit(false);
            final PreparedStatement stmt = this.prepareStatement(sql, variables);
            stmt.executeUpdate();
            this.getConnection().setAutoCommit(autoCommit);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    public ResultSet query(final String sql, final Object... variables)
    {
        try
        {
            final PreparedStatement stmt = this.prepareStatement(sql, variables);
            final ResultSet rs = stmt.executeQuery();
            stmt.closeOnCompletion();
            return rs;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    public PreparedStatement prepareStatement(final String sql, final Object... variables)
    {
        try
        {
            if (!isConnected())
                connect();
            final PreparedStatement stmt = this.getConnection().prepareStatement(sql);
            setVariables(stmt, variables);
            return stmt;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    private static void setVariables(final PreparedStatement stmt, final Object... variables) throws SQLException
    {
        for (int i = 1; i < variables.length + 1; i++)
        {
            final Object var = variables[i - 1];
            if (var == null)
                stmt.setNull(i, getSQLType(var));
            switch (getSQLType(var))
            {
                
                case Types.LONGVARCHAR:
                case Types.VARCHAR:
                    stmt.setString(i, (String) var);
                    break;
                case Types.BIT:
                case Types.BOOLEAN:
                    stmt.setBoolean(i, (boolean) var);
                    break;
                case Types.TINYINT:
                    stmt.setByte(i, (byte) var);
                    break;
                case Types.LONGVARBINARY:
                case Types.VARBINARY:
                    stmt.setBytes(i, (byte[]) var);
                    break;
                case Types.SMALLINT:
                    stmt.setShort(i, (short) var);
                    break;
                case Types.INTEGER:
                    stmt.setInt(i, (int) var);
                    break;
                case Types.BIGINT:
                    stmt.setLong(i, (long) var);
                    break;
                case Types.REAL:
                    stmt.setFloat(i, (float) var);
                    break;
                case Types.DOUBLE:
                    stmt.setDouble(i, (double) var);
                    break;
                case Types.NUMERIC:
                    stmt.setBigDecimal(i, (BigDecimal) var);
                    break;
                case Types.ARRAY:
                    stmt.setArray(i, (Array) var);
                    break;
                case Types.BLOB:
                    stmt.setBlob(i, (Blob) var);
                    break;
                case Types.CLOB:
                    stmt.setClob(i, (Clob) var);
                    break;
                case Types.NCLOB:
                    stmt.setNClob(i, (NClob) var);
                    break;
                case Types.DATE:
                    stmt.setDate(i, (Date) var);
                    break;
                case Types.TIME:
                    stmt.setTime(i, (Time) var);
                    break;
                case Types.TIMESTAMP:
                    stmt.setTimestamp(i, (Timestamp) var);
                    break;
                case Types.REF:
                    stmt.setRef(i, (Ref) var);
                    break;
                case Types.ROWID:
                    stmt.setRowId(i, (RowId) var);
                    break;
                case Types.SQLXML:
                    stmt.setSQLXML(i, (SQLXML) var);
                    break;
                case Types.DATALINK:
                    stmt.setURL(i, (URL) var);
                    break;
                
                default:
                    stmt.setObject(i, var);
            }
        }
    }
    
    private static int getSQLType(final Object var)
    {
        if (var instanceof String)
            return Types.VARCHAR;
        if (var instanceof Boolean)
            return Types.BOOLEAN;
        if (var instanceof Byte)
            return Types.TINYINT;
        if (var instanceof byte[])
            return Types.VARBINARY;
        if (var instanceof Short)
            return Types.SMALLINT;
        if (var instanceof Integer)
            return Types.INTEGER;
        if (var instanceof Long)
            return Types.BIGINT;
        if (var instanceof Float)
            return Types.REAL;
        if (var instanceof Double)
            return Types.DOUBLE;
        if (var instanceof BigDecimal)
            return Types.NUMERIC;
        if (var instanceof Array)
            return Types.ARRAY;
        if (var instanceof Blob)
            return Types.BLOB;
        if (var instanceof Clob)
            return Types.CLOB;
        if (var instanceof NClob)
            return Types.NCLOB;
        if (var instanceof Date)
            return Types.DATE;
        if (var instanceof Time)
            return Types.TIME;
        if (var instanceof Timestamp)
            return Types.TIMESTAMP;
        if (var instanceof Ref)
            return Types.REF;
        if (var instanceof RowId)
            return Types.ROWID;
        if (var instanceof SQLXML)
            return Types.SQLXML;
        if (var instanceof URL)
            return Types.DATALINK;
        
        return Types.OTHER;
    }
    
}
