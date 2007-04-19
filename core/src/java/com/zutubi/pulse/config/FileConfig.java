package com.zutubi.pulse.config;

import com.zutubi.util.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * <class-comment/>
 */
public class FileConfig implements Config
{
    private static final Logger LOG = Logger.getLogger(FileConfig.class);

    private final File file;

    private Properties props;

    public FileConfig(File file)
    {
        this.file = file;
    }

    public String getProperty(String key)
    {
        return getProperties().getProperty(key);
    }

    public void setProperty(String key, String value)
    {
        getProperties().setProperty(key, value);
        writeToFile();
    }

    private void writeToFile()
    {
        try
        {
            if (!file.isFile())
            {
                if (!file.getParentFile().isDirectory() && !file.getParentFile().mkdirs())
                {
                    throw new IOException();
                }
                if (!file.createNewFile())
                {
                    throw new IOException();
                }
            }
            PropertiesWriter writer = new PropertiesWriter();
            writer.write(file, getProperties());
        }
        catch (IOException e)
        {
            LOG.error(e);
        }
    }

    public boolean hasProperty(String key)
    {
        return getProperties().containsKey(key);
    }

    public void removeProperty(String key)
    {
        getProperties().remove(key);
        writeToFile();
    }

    private Properties getProperties()
    {
        if (props == null)
        {
            try
            {
                if (file.isFile())
                {
                    props = IOUtils.read(file);
                }
                else
                {
                    props = new Properties();
                }
            }
            catch (IOException e)
            {
                LOG.severe(e);
                props = new Properties();
            }
        }

        return props;
    }

    public boolean isWriteable()
    {
        return true;
    }
}
