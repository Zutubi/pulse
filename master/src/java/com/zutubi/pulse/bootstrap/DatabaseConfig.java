package com.zutubi.pulse.bootstrap;

import java.util.Properties;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

/**
 *
 *
 */
public class DatabaseConfig
{
    private final Properties properties;

    public DatabaseConfig(Properties config)
    {
        this.properties = config;
    }

    public Properties getProperties()
    {
        return properties;
    }

    public String getDriverClassName()
    {
        return properties.getProperty("jdbc.driverClassName");
    }

    public String getUrl()
    {
        return properties.getProperty("jdbc.url");
    }

    public String getUsername()
    {
        return properties.getProperty("jdbc.username");
    }
    
    public String getPassword()
    {
        return properties.getProperty("jdbc.password");
    }

    public boolean isEmbedded()
    {
        return getDriverClassName().contains(".hsqldb.");
    }

    public Map<String, String> getConnectionProperties()
    {
        Map<String, String> connectionProperties = new HashMap<String, String>();
        Iterator propertyNames = properties.keySet().iterator();
        while (propertyNames.hasNext())
        {
            String propertyName = (String) propertyNames.next();
            if (propertyName.startsWith("jdbc.property."))
            {
                String key = propertyName.substring(14);
                String value = properties.getProperty(propertyName);
                connectionProperties.put(key, value);
            }
        }
        return connectionProperties;
    }

    public Properties getHibernateProperties()
    {
        Properties hibernateProperties = new Properties();
        Iterator propertyNames = properties.keySet().iterator();
        while (propertyNames.hasNext())
        {
            String propertyName = (String) propertyNames.next();
            if (propertyName.startsWith("hibernate."))
            {
                String value = properties.getProperty(propertyName);
                hibernateProperties.put(propertyName, value);
            }
        }
        return hibernateProperties;
    }
}
