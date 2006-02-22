package com.cinnamonbob.bootstrap.config;

import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * <class-comment/>
 */
public class FileConfiguration implements Configuration, Editable
{
    private static final Logger LOG = Logger.getLogger(FileConfiguration.class);

    private final File file;

    private Properties props;
    private long lastModified;

    public FileConfiguration(File file)
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
            if (!file.exists())
            {
                if (!file.getParentFile().mkdirs() && !file.createNewFile())
                {
                    throw new IOException();
                }
                lastModified = file.lastModified();
            }
            IOUtils.write(getProperties(), file);
            lastModified = file.lastModified();
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
        if (props == null || file.lastModified() != lastModified)
        {
            try
            {
                if (file.exists())
                {
                    lastModified = file.lastModified();
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
}
