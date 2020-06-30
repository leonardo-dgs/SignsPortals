package net.leonardo_dgs.signsportals;

import co.aikar.idb.DB;
import lombok.SneakyThrows;

import static java.lang.String.format;

final class DatabaseQueries {

    private static final String TABLE_PREFIX = SignsPortals.getInstance().getConfig().getString("database.table_prefix");

    private static final String CREATE_PORTALS_TABLE =
            format("CREATE TABLE IF NOT EXISTS `%sportals` (`location` VARCHAR(64) PRIMARY KEY, `owner` INTEGER UNSIGNED NOT NULL, `name` VARCHAR(255) NOT NULL, `destination` VARCHAR(255) NOT NULL)", TABLE_PREFIX);
    private static final String MYSQL_CREATE_PLAYERS_TABLE =
            format("CREATE TABLE IF NOT EXISTS `%splayers` (`id` INTEGER UNSIGNED PRIMARY KEY AUTO_INCREMENT, `username` VARCHAR(16) NOT NULL, `uuid` CHAR(36) UNIQUE NOT NULL)", TABLE_PREFIX);
    private static final String SQLITE_CREATE_PLAYERS_TABLE =
            format("CREATE TABLE IF NOT EXISTS `%splayers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `username` VARCHAR(16) NOT NULL, `uuid` CHAR(36) UNIQUE NOT NULL)", TABLE_PREFIX);
    private static final String MYSQL_CREATE_WORLDS_TABLE =
            format("CREATE TABLE IF NOT EXISTS `%sworlds` (`id` INTEGER UNSIGNED PRIMARY KEY AUTO_INCREMENT, `world_uid` CHAR(36) UNIQUE NOT NULL)", TABLE_PREFIX);
    private static final String SQLITE_CREATE_WORLDS_TABLE =
            format("CREATE TABLE IF NOT EXISTS `%sworlds` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `world_uid` CHAR(36) UNIQUE NOT NULL)", TABLE_PREFIX);

    static final String INSERT_WORLD =
            format("INSERT INTO `%sworlds` (`world_uid`) VALUES (?)", TABLE_PREFIX);
    static final String INSERT_PLAYER =
            format("INSERT INTO `%splayers` (`username`, `uuid`) VALUES (?, ?)", TABLE_PREFIX);
    static final String UPDATE_PLAYER_USERNAME =
            format("UPDATE `%splayers` SET `username` = ? WHERE `id` = ?", TABLE_PREFIX);

    static final String GET_WORLD_UID =
            format("SELECT `world_uid` FROM `%sworlds` WHERE `id` = ? LIMIT 1", TABLE_PREFIX);
    static final String GET_PLAYER_UUID =
            format("SELECT `uuid` FROM `%splayers` WHERE `id` = ? LIMIT 1", TABLE_PREFIX);
    static final String GET_PLAYER_USERNAME =
            format("SELECT `username` FROM `%splayers` WHERE `id` = ? LIMIT 1", TABLE_PREFIX);
    static final String GET_PLAYER_USERNAME_FROM_UUID =
            format("SELECT `username` FROM `%splayers` WHERE `uuid` = ? LIMIT 1", TABLE_PREFIX);

    static final String GET_WORLD_ID_FROM_UID =
            format("SELECT `id` FROM `%sworlds` WHERE `world_uid` = ? LIMIT 1", TABLE_PREFIX);
    static final String GET_PLAYER_ID_FROM_UUID =
            format("SELECT `id` FROM `%splayers` WHERE `uuid` = ? LIMIT 1", TABLE_PREFIX);
    static final String GET_PLAYER_ID_FROM_USERNAME =
            format("SELECT `id` FROM `%splayers` WHERE `username` = ? LIMIT 1", TABLE_PREFIX);

    static final String INSERT_PORTAL =
            format("REPLACE INTO `%sportals` (`location`, `owner`, `name`, `destination`) VALUES (?, ?, ?, ?)", TABLE_PREFIX);
    static final String DELETE_PORTAL =
            format("DELETE FROM `%sportals` WHERE `location` = ?", TABLE_PREFIX);
    static final String GET_PORTAL_FROM_BLOCK =
            format("SELECT `owner`, `name`, `destination` FROM `%sportals` WHERE `location` = ? LIMIT 1", TABLE_PREFIX);
    static final String GET_PORTAL_FROM_OWNER_AND_NAME =
            format("SELECT `location`, `destination` FROM `%sportals` WHERE `owner` = ? AND `name` = ? LIMIT 1", TABLE_PREFIX);
    static final String GET_PLAYER_PORTALS =
            format("SELECT `location`, `name`, `destination` FROM `%sportals` WHERE `owner` = ?", TABLE_PREFIX);

    @SneakyThrows
    static void createSchema(String databaseType)
    {
        DB.executeUpdate(CREATE_PORTALS_TABLE);
        switch (databaseType)
        {
            default:
            case "SQLITE":
                DB.executeUpdate(SQLITE_CREATE_PLAYERS_TABLE);
                DB.executeUpdate(SQLITE_CREATE_WORLDS_TABLE);
                break;
            case "MYSQL":
                DB.executeUpdate(MYSQL_CREATE_PLAYERS_TABLE);
                DB.executeUpdate(MYSQL_CREATE_WORLDS_TABLE);
                break;
        }
    }

}
