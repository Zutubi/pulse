package com.zutubi.pulse.prototype.config.setup;

import static com.zutubi.pulse.database.DatabaseConfig.*;

import java.util.Properties;

/**
 */
public enum DatabaseType
{
    EMBEDDED
    {
        public boolean isEmbedded()
        {
            return true;
        }

        public Properties getDatabaseProperties(SetupDatabaseTypeConfiguration config)
        {
            return getStandardProperties("org.hsqldb.jdbcDriver", "jdbc:hsqldb:DB_ROOT/db", "sa", "", "org.hibernate.dialect.HSQLDialect");
        }
    },
    MYSQL
    {
        public boolean isEmbedded()
        {
            return false;
        }

        public Properties getDatabaseProperties(SetupDatabaseTypeConfiguration config)
        {
            return getStandardProperties("com.mysql.jdbc.Driver", "jdbc:mysql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase() + "?autoReconnect=true", config.getUser(), config.getPassword(), "org.hibernate.dialect.MySQLDialect");
        }
    },
    POSTGRESQL
    {
        public boolean isEmbedded()
        {
            return false;
        }

        public Properties getDatabaseProperties(SetupDatabaseTypeConfiguration config)
        {
            return getStandardProperties("org.postgresql.Driver", "jdbc:postgresql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase(), config.getUser(), config.getPassword(), "org.hibernate.dialect.PostgreSQLDialect");
        }
    },
    RUBBISH
    {
        public boolean isEmbedded()
        {
            return false;
        }

        public Properties getDatabaseProperties(SetupDatabaseTypeConfiguration config)
        {
            return getStandardProperties("org.postgresql.Driver", "jdbc:postgresql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase(), config.getUser(), config.getPassword(), "org.hibernate.dialect.PostgreSQLDialect");
        }
    },
    ;

    public abstract boolean isEmbedded();
    public abstract Properties getDatabaseProperties(SetupDatabaseTypeConfiguration config);

    public String getJDBCClassName(SetupDatabaseTypeConfiguration config)
    {
        return getDatabaseProperties(config).getProperty(JDBC_DRIVER_CLASS_NAME);
    }

    public String getJDBCUrl(SetupDatabaseTypeConfiguration configuration)
    {
        return getDatabaseProperties(configuration).getProperty(JDBC_URL);
    }

    public Properties getStandardProperties(String jdbcClassName, String jdbcUrl, String jdbcUsername, String jdbcPassword, String hibernateDialect)
    {
        Properties result = new Properties();
        result.put(JDBC_DRIVER_CLASS_NAME, jdbcClassName);
        result.put(JDBC_URL, jdbcUrl);
        result.put(JDBC_USERNAME, jdbcUsername);
        result.put(JDBC_PASSWORD, jdbcPassword);
        result.put(HIBERNATE_DIALECT, hibernateDialect);
        return result;
    }
}
