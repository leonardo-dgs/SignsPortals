package net.leomixer17.signsportals.database;

public enum DatabaseType {
	
	H2("org.h2.Driver", "jdbc:h2:%database%"),
	SQLITE("org.sqlite.JDBC", "jdbc:sqlite:%database%"),
	MYSQL("com.mysql.jdbc.Driver", "jdbc:mysql://%host%:%port%/%database%%properties%"),
	POSTGRESQL("org.postgresql.Driver", "jdbc:postgresql://%host%:%port%/%database%%properties%"),
	DB2("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://%host%:%port%/%database%");
	
	private final String driver;
	private final String urlFormat;
	
	DatabaseType(final String driver, final String urlFormat)
	{
		this.driver = driver;
		this.urlFormat = urlFormat;
	}
	
	public String getDriver()
	{
		return this.driver;
	}
	
	public String getUrlFormat()
	{
		return this.urlFormat;
	}
	
}