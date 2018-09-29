package net.leomixer17.signsportals.database;

import java.sql.PreparedStatement;

import net.leomixer17.signsportals.SignsPortals;

public final class DatabaseManager extends SQLDatabase {
	
	private static final String CREATE_PORTALS_TABLE = 
		"CREATE TABLE IF NOT EXISTS `${table_prefix}portals` (`location` VARCHAR(64) PRIMARY KEY, `owner` INTEGER UNSIGNED NOT NULL, `name` VARCHAR(255) NOT NULL, `destination` VARCHAR(255) NOT NULL)";
	private static final String MYSQL_CREATE_PLAYERS_TABLE = 
		"CREATE TABLE IF NOT EXISTS `${table_prefix}players` (`id` INTEGER UNSIGNED PRIMARY KEY AUTO_INCREMENT, `username` VARCHAR(16) NOT NULL, `uuid` CHAR(36) UNIQUE NOT NULL)";
	private static final String SQLITE_CREATE_PLAYERS_TABLE = 
		"CREATE TABLE IF NOT EXISTS `${table_prefix}players` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `username` VARCHAR(16) NOT NULL, `uuid` CHAR(36) UNIQUE NOT NULL)";
	private static final String MYSQL_CREATE_WORLDS_TABLE = 
		"CREATE TABLE IF NOT EXISTS `${table_prefix}worlds` (`id` INTEGER UNSIGNED PRIMARY KEY AUTO_INCREMENT, `world_uid` CHAR(36) UNIQUE NOT NULL)";
	private static final String SQLITE_CREATE_WORLDS_TABLE = 
		"CREATE TABLE IF NOT EXISTS `${table_prefix}worlds` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `world_uid` CHAR(36) UNIQUE NOT NULL)";
	
	public static final String INSERT_WORLD = 
		"INSERT INTO `${table_prefix}worlds` (`world_uid`) VALUES (?)";
	public static final String INSERT_PLAYER = 
		"INSERT INTO `${table_prefix}players` (`username`, `uuid`) VALUES (?, ?)";
	public static final String UPDATE_PLAYER_USERNAME = 
		"UPDATE `${table_prefix}players` SET `username` = ? WHERE `id` = ?";
	
	public static final String GET_WORLD_UID = 
		"SELECT `world_uid` FROM `${table_prefix}worlds` WHERE `id` = ? LIMIT 1";
	public static final String GET_PLAYER_UUID = 
		"SELECT `uuid` FROM `${table_prefix}players` WHERE `id` = ? LIMIT 1";
	public static final String GET_PLAYER_USERNAME = 
		"SELECT `username` FROM `${table_prefix}players` WHERE `id` = ? LIMIT 1";
	public static final String GET_PLAYER_USERNAME_FROM_UUID = 
		"SELECT `username` FROM `${table_prefix}players` WHERE `uuid` = ? LIMIT 1";
	
	public static final String GET_WORLD_ID_FROM_UID = 
		"SELECT `id` FROM `${table_prefix}worlds` WHERE `world_uid` = ? LIMIT 1";
	public static final String GET_PLAYER_ID_FROM_UUID = 
		"SELECT `id` FROM `${table_prefix}players` WHERE `uuid` = ? LIMIT 1";
	public static final String GET_PLAYER_ID_FROM_USERNAME = 
		"SELECT `id` FROM `${table_prefix}players` WHERE `username` = ? LIMIT 1";
	
	public static final String INSERT_PORTAL = 
		"REPLACE INTO `${table_prefix}portals` (`location`, `owner`, `name`, `destination`) VALUES (?, ?, ?, ?)";
	public static final String DELETE_PORTAL = 
		"DELETE FROM `${table_prefix}portals` WHERE `location` = ?";
	public static final String GET_PORTAL_FROM_BLOCK = 
		"SELECT `owner`, `name`, `destination` FROM `${table_prefix}portals` WHERE `location` = ? LIMIT 1";
	public static final String GET_PORTAL_FROM_OWNER_AND_NAME = 
		"SELECT `location`, `destination` FROM `${table_prefix}portals` WHERE `owner` = ? AND `name` = ? LIMIT 1";
	public static final String GET_PLAYER_PORTALS = 
		"SELECT `location`, `name`, `destination` FROM `${table_prefix}portals` WHERE `owner` = ?";
	
	public DatabaseManager(final DatabaseType type, final DatabaseConnectionSettings settings)
	{
		super(type, settings);
	}
	
	public void initialise()
	{
		this.connect();
		this.createSchema();
	}
	
	public void finalise()
	{
		this.disconnect();
	}
	
	private void createSchema()
	{
		this.update(CREATE_PORTALS_TABLE);
		if(this.getType().equals(DatabaseType.MYSQL)) {
			this.update(MYSQL_CREATE_PLAYERS_TABLE);
			this.update(MYSQL_CREATE_WORLDS_TABLE);
		}
		else {
			this.update(SQLITE_CREATE_PLAYERS_TABLE);
			this.update(SQLITE_CREATE_WORLDS_TABLE);
		}
	}
	
	@Override
	public PreparedStatement prepareStatement(String sql, final Object... variables)
	{
		return super.prepareStatement(sql.replaceFirst("\\$\\{table_prefix\\}", SignsPortals.getPlugin().getConfig().getString("database.table_prefix")), variables);
	}
	
}
