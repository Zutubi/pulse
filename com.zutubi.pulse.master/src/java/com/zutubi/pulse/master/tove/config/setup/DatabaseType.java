package com.zutubi.pulse.master.tove.config.setup;

import com.zutubi.pulse.master.migrate.MigrateDatabaseTypeConfiguration;
import com.zutubi.tove.ui.forms.EnumOptionProvider;

import java.util.Properties;

import static com.zutubi.pulse.master.database.DatabaseConfig.*;

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

        public Properties getDatabaseProperties(MigrateDatabaseTypeConfiguration config)
        {
            return getStandardProperties(config.getDriver(), "jdbc:hsqldb:DB_ROOT/db", "sa", "", "org.hibernate.dialect.HSQLDialect");
        }

        public MigrateDatabaseTypeConfiguration getDatabaseConfiguration(Properties props)
        {
            MigrateDatabaseTypeConfiguration config = new MigrateDatabaseTypeConfiguration();
            config.setDriver(props.getProperty(JDBC_DRIVER_CLASS_NAME));
            config.setPassword(props.getProperty(JDBC_PASSWORD));
            config.setUser(props.getProperty(JDBC_USERNAME));
            String url = props.getProperty(JDBC_URL);
            config.setDatabase(url.substring(12));
            config.setType(this);
            return config;
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

        public Properties getDatabaseProperties(MigrateDatabaseTypeConfiguration config)
        {
            return getStandardProperties(config.getDriver(), "jdbc:mysql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase() + "?autoReconnect=true", config.getUser(), config.getPassword(), "org.hibernate.dialect.MySQLDialect");
        }

        public MigrateDatabaseTypeConfiguration getDatabaseConfiguration(Properties props)
        {
            MigrateDatabaseTypeConfiguration config = new MigrateDatabaseTypeConfiguration();
            config.setDriver(props.getProperty(JDBC_DRIVER_CLASS_NAME));
            config.setPassword(props.getProperty(JDBC_PASSWORD));
            config.setUser(props.getProperty(JDBC_USERNAME));
            config.setType(this);

            String url = props.getProperty(JDBC_URL);
            config.setHost(url.substring(13, url.indexOf(':', 13)));
            int indexOfColon = url.indexOf(':', 13) + 1;
            config.setPort(Integer.valueOf(url.substring(indexOfColon, url.indexOf('/', indexOfColon))));
            config.setDatabase(url.substring(url.indexOf('/', indexOfColon) + 1, url.indexOf('?', indexOfColon)));
            return config;
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

        public Properties getDatabaseProperties(MigrateDatabaseTypeConfiguration config)
        {
            return getStandardProperties(config.getDriver(), "jdbc:postgresql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase(), config.getUser(), config.getPassword(), "org.hibernate.dialect.PostgreSQLDialect");
        }

        public MigrateDatabaseTypeConfiguration getDatabaseConfiguration(Properties props)
        {
            MigrateDatabaseTypeConfiguration config = new MigrateDatabaseTypeConfiguration();
            config.setDriver(props.getProperty(JDBC_DRIVER_CLASS_NAME));
            config.setPassword(props.getProperty(JDBC_PASSWORD));
            config.setUser(props.getProperty(JDBC_USERNAME));
            config.setType(this);

            String url = props.getProperty(JDBC_URL);
            // trim the front.
            url = url.substring(18);
            config.setHost(url.substring(0, url.indexOf(':')));
            url = url.substring(url.indexOf(":") + 1);
            config.setPort(Integer.valueOf(url.substring(0, url.indexOf("/"))));
            url = url.substring(url.indexOf("/") + 1);
            config.setDatabase(url);
            return config;
        }
    }
    ;

    public abstract boolean isEmbedded();
    public abstract Properties getDatabaseProperties(SetupDatabaseTypeConfiguration config);
    public abstract Properties getDatabaseProperties(MigrateDatabaseTypeConfiguration config);
    public abstract MigrateDatabaseTypeConfiguration getDatabaseConfiguration(Properties props);

    public String getJDBCClassName(SetupDatabaseTypeConfiguration config)
    {
        return getDatabaseProperties(config).getProperty(JDBC_DRIVER_CLASS_NAME);
    }

    public String getJDBCClassName(MigrateDatabaseTypeConfiguration config)
    {
        return config.getDriver();
    }

    public String getJDBCUrl(SetupDatabaseTypeConfiguration configuration)
    {
        return getDatabaseProperties(configuration).getProperty(JDBC_URL);
    }

    public String getJDBCUrl(MigrateDatabaseTypeConfiguration configuration)
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

    public String getPrettyName()
    {
        return EnumOptionProvider.getPrettyName(this);
    }
}
