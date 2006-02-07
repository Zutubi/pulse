package com.cinnamonbob.bootstrap;

import java.util.Properties;
import java.io.OutputStream;
import java.io.IOException;

/**
 * <class-comment/>
 */
public class ReadWriteConfiguration implements Configuration
{
    private final Properties readOnly;
    private final Properties readWrite;

    public ReadWriteConfiguration()
    {
        readOnly = new Properties();
        readWrite = new Properties();
    }

    public ReadWriteConfiguration(Properties defaults)
    {
        this.readOnly = defaults;
        this.readWrite = new Properties();
    }

    public ReadWriteConfiguration(Properties defaults, Properties properties)
    {
        this.readOnly = defaults;
        this.readWrite = properties;
    }

    public String getProperty(String key)
    {
        String prop = readWrite.getProperty(key);
        if (prop == null)
        {
            prop = readOnly.getProperty(key);
        }
        return prop;
    }

    public boolean hasProperty(String key)
    {
        return readWrite.getProperty(key) != null || readOnly.getProperty(key) != null;
    }

    public void resetDefaults()
    {
        readWrite.clear();
    }

    public void setProperty(String key, String value)
    {
        if (value != null)
        {
            readWrite.setProperty(key, value);
        }
        else
        {
            readWrite.remove(key);
        }
    }

    public void save(OutputStream output) throws IOException
    {
        readWrite.store(output, "This is an auto generated properties file. " +
                "Any changes made directly to this file may be lost.");
    }
}
