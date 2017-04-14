/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.database;

import com.zutubi.pulse.servercore.bootstrap.MasterUserPaths;
import com.zutubi.util.Constants;
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

    protected static final String POOL_INITIAL_SIZE                      = "pool.initialSize";
    protected static final String POOL_MAX_ACTIVE                        = "pool.maxActive";
    protected static final String POOL_MAX_IDLE                          = "pool.maxIdle";
    protected static final String POOL_MIN_IDLE                          = "pool.minIdle";
    protected static final String POOL_MAX_WAIT                          = "pool.maxWait";
    protected static final String POOL_VALIDATION_QUERY                  = "pool.validationQuery";
    protected static final String POOL_TEST_ON_BORROW                    = "pool.testOnBorrow";
    protected static final String POOL_TEST_WHILE_IDLE                   = "pool.testWhileIdle";
    protected static final String POOL_TIME_BETWEEN_EVICTION_RUNS_MILLIS = "pool.timeBetweenEvictionRunsMillis";
    protected static final String POOL_NUM_TESTS_PER_EVICTION_RUN        = "pool.numTestsPerEvictionRun";
    protected static final String POOL_MIN_EVICTABLE_IDLE_TIME_MILLIS    = "pool.minEvictableIdleTimeMillis";

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
        return getIntProperty(POOL_MAX_ACTIVE, 256);
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

    public String getPoolValidationQuery()
    {
        // Special case the default for HSQL, where there is no sensible query
        // before we have created our own schema, and validation is useless in
        // any case.
        return properties.getProperty(POOL_VALIDATION_QUERY, getUrl().contains(":hsqldb:") ? "" : "SELECT 1");
    }
    
    public boolean getPoolTestOnBorrow()
    {
        return getBooleanProperty(POOL_TEST_ON_BORROW, false);
    }

    public boolean getPoolTestWhileIdle()
    {
        return getBooleanProperty(POOL_TEST_WHILE_IDLE, true);
    }

    public int getPoolTimeBetweenEvictionRunsMillis()
    {
        return getIntProperty(POOL_TIME_BETWEEN_EVICTION_RUNS_MILLIS, 30 * (int)Constants.MINUTE);
    }
    
    public int getPoolNumTestsPerEvitionRun()
    {
        return getIntProperty(POOL_NUM_TESTS_PER_EVICTION_RUN, 3);
    }
    
    public int getPoolMinEvictableIdleTimeMillis()
    {
        return getIntProperty(POOL_MIN_EVICTABLE_IDLE_TIME_MILLIS, 10 * (int)Constants.MINUTE);
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

    private boolean getBooleanProperty(String name, boolean defaultValue)
    {
        String value = properties.getProperty(name);
        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            return Boolean.valueOf(value);
        }
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

        // Enable second level caching, required since by default it is disabled.
        hibernateProperties.put("hibernate.cache.use_second_level_cache", "true");
        hibernateProperties.put("hibernate.cache.provider_class", "org.hibernate.cache.EhCacheProvider");
        hibernateProperties.put("hibernate.cache.use_query_cache", "false"); // enable/disable the query cache

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

    /**
     * Creates a data source configured by our settings.
     *
     * @param specifyDriverClass true if we should specify the driver class name, false otherwise.
     *                           This should be false if the driver is loaded dynamically and
     *                           registered with the DriverManager.  It should only be true when
     *                           the driver class needs loading directly by the data source.
     * @return the configured data source
     */
    public BasicDataSource createDataSource(boolean specifyDriverClass)
    {
        BasicDataSource dataSource = new BasicDataSource();

        if (specifyDriverClass)
        {
            dataSource.setDriverClassName(getDriverClassName());
        }
        dataSource.setUrl(getUrl());
        dataSource.setUsername(getUsername());
        dataSource.setPassword(getPassword());

        dataSource.setInitialSize(getPoolInitialSize());
        dataSource.setMaxActive(getPoolMaxActive());
        dataSource.setMaxIdle(getPoolMaxIdle());
        dataSource.setMinIdle(getPoolMinIdle());
        dataSource.setMaxWait(getPoolMaxWait());
        dataSource.setValidationQuery(getPoolValidationQuery());
        dataSource.setTestOnBorrow(getPoolTestOnBorrow());
        dataSource.setTestWhileIdle(getPoolTestWhileIdle());
        dataSource.setTimeBetweenEvictionRunsMillis(getPoolTimeBetweenEvictionRunsMillis());
        dataSource.setNumTestsPerEvictionRun(getPoolNumTestsPerEvitionRun());
        dataSource.setMinEvictableIdleTimeMillis(getPoolMinEvictableIdleTimeMillis());
        
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
