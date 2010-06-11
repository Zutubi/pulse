package com.zutubi.pulse.master.database;

import com.zutubi.pulse.servercore.bootstrap.MasterUserPaths;
import com.zutubi.util.logging.Logger;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.cfg.Environment;

import java.util.Properties;

/**
 * The database configuration object represents all of the connection configuration details.
 */
public class DatabaseConfig
{
    public static final String JDBC_DRIVER_CLASS_NAME = "jdbc.driverClassName";
    public static final String JDBC_URL               = "jdbc.url";
    public static final String JDBC_USERNAME          = "jdbc.username";
    public static final String JDBC_PASSWORD          = "jdbc.password";

    public static final String HIBERNATE_DIALECT      = Environment.DIALECT;

    protected static final String POOL_INITIAL_SIZE = "pool.initialSize";
    protected static final String POOL_MAX_ACTIVE   = "pool.maxActive";
    protected static final String POOL_MAX_IDLE     = "pool.maxIdle";
    protected static final String POOL_MIN_IDLE     = "pool.minIdle";
    protected static final String POOL_MAX_WAIT     = "pool.maxWait";

    protected static final String JDBC_PROPERTY_PREFIX      = "jdbc.property.";
    protected static final String HIBERNATE_PROPERTY_PREFIX = "hibernate.";

    private static final Logger LOG = Logger.getLogger(DatabaseConfig.class);

    /**
     * The internal configuration store.
     */
    private final Properties properties;

    private MasterUserPaths userPaths;

    public DatabaseConfig(Properties config)
    {
        this.properties = config;
    }

    public Properties getProperties()
    {
        return properties;
    }

    /**
     * The name of the jdbc driver class to be used to handle communications with the
     * database.
     *
     * @return the classname.
     */
    public String getDriverClassName()
    {
        return properties.getProperty(JDBC_DRIVER_CLASS_NAME);
    }

    /**
     * The JDBC connection URL.
     *
     * @return the jdbc url.
     */
    public String getUrl()
    {
        String url = properties.getProperty(JDBC_URL);
        if (url.contains("DB_ROOT") && userPaths != null)
        {
            // process the substitution iff the user paths is available.
            url = url.replace("DB_ROOT", userPaths.getDatabaseRoot().getAbsolutePath());
        }
        return url;
    }

    /**
     * The jdbc connection authorization username.
     *
     * @return username
     */
    public String getUsername()
    {
        return properties.getProperty(JDBC_USERNAME);
    }
    
    /**
     * The jdbc connection authorization password.
     *
     * @return password
     */
    public String getPassword()
    {
        return properties.getProperty(JDBC_PASSWORD);
    }

    public int getPoolInitialSize()
    {
        return getIntProperty(POOL_INITIAL_SIZE, 0);
    }

    public int getPoolMaxActive()
    {
        return getIntProperty(POOL_MAX_ACTIVE, 32);
    }

    public int getPoolMaxIdle()
    {
        return getIntProperty(POOL_MAX_IDLE, 8);
    }

    public int getPoolMinIdle()
    {
        return getIntProperty(POOL_MIN_IDLE, 0);
    }

    public int getPoolMaxWait()
    {
        return getIntProperty(POOL_MAX_WAIT, -1);
    }

    private int getIntProperty(String name, int defaultValue)
    {
        int result = defaultValue;
        String value = properties.getProperty(name);
        if(value != null)
        {
            try
            {
                result = Integer.parseInt(value);
            }
            catch(NumberFormatException e)
            {
                LOG.warning("Unable to parse integer property '" + name + "', value '" + value + "', using default (" + defaultValue + ")");
            }
        }

        return result;
    }

    /**
     * Retrieve the connection related properties. These are the properties with the
     * jdbc.property prefix. The returned map will contain the keys with the prefix
     * removed.
     *
     * @return a map of the connection properties.
     */
    public Properties getConnectionProperties()
    {
        Properties props = new Properties();
        for (Object o : properties.keySet())
        {
            String propertyName = (String) o;
            if (propertyName.startsWith(JDBC_PROPERTY_PREFIX))
            {
                String key = propertyName.substring(JDBC_PROPERTY_PREFIX.length());
                String value = properties.getProperty(propertyName);
                props.put(key, value);
            }
        }
        return props;
    }

    /**
     * Retrieve the hibernate related properties. These are the properties with the
     * hiberate prefix.
     *
     * @return a map of the hibernate properties.
     */
    public Properties getHibernateProperties()
    {
        Properties hibernateProperties = new Properties();
        for (Object o : properties.keySet())
        {
            String propertyName = (String) o;
            if (propertyName.startsWith(HIBERNATE_PROPERTY_PREFIX))
            {
                String value = properties.getProperty(propertyName);
                hibernateProperties.put(propertyName, value);
            }
        }
        return hibernateProperties;
    }

    /**
     * Set the user paths resource, used to handle substitution of variables in the
     * database configuration.
     *
     * @param userPaths instance.
     */
    public void setUserPaths(MasterUserPaths userPaths)
    {
        this.userPaths = userPaths;
    }

    public BasicDataSource createDataSource()
    {
        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setDriverClassName(getDriverClassName());
        dataSource.setUrl(getUrl());
        dataSource.setUsername(getUsername());
        dataSource.setPassword(getPassword());

        dataSource.setInitialSize(getPoolInitialSize());
        dataSource.setMaxActive(getPoolMaxActive());
        dataSource.setMaxIdle(getPoolMaxIdle());
        dataSource.setMinIdle(getPoolMinIdle());
        dataSource.setMaxWait(getPoolMaxWait());

        // configure the dataSource using the custom connection properties.
        Properties connectionProperties = getConnectionProperties();
        for (Object o : connectionProperties.keySet())
        {
            String propertyName = (String) o;
            dataSource.addConnectionProperty(propertyName, connectionProperties.getProperty(propertyName));
        }

        return dataSource;

    }
}
