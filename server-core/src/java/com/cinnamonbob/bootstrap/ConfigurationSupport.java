package com.cinnamonbob.bootstrap;

/**
 * <class-comment/>
 */
public class ConfigurationSupport
{
    private static final String TEST_PROPERTY = "this.is.a.test.property.and.should.not.be.used.for.real.data";

    private final Configuration[] configs;
    private Configuration writeable;

    public ConfigurationSupport(Configuration... configs)
    {
        this.configs = configs;

        // determine the first writable configuration.
        for (Configuration config : configs)
        {
            try
            {
                config.setProperty(TEST_PROPERTY, "");
                writeable = config;
                writeable.setProperty(TEST_PROPERTY, null);
                break;
            }
            catch (UnsupportedOperationException e)
            {
                // noop.
            }
        }
    }

    private Configuration getConfig(String key)
    {
        for (Configuration config : configs)
        {
            if (config.hasProperty(key))
            {
                return config;
            }
        }
        return null;
    }

    public boolean hasProperty(String key)
    {
        return getConfig(key) != null;
    }

    public String getProperty(String key)
    {
        Configuration config = getConfig(key);
        if (config != null)
        {
            return config.getProperty(key);
        }
        return null;
    }

    public void setProperty(String key, String value)
    {
        if (writeable == null)
        {
            throw new UnsupportedOperationException();
        }
        writeable.setProperty(key, value);
    }

    public int getInt(String key)
    {
        return Integer.parseInt(getProperty(key));
    }

    public void setInt(String key, int value)
    {
        setProperty(key, Integer.toString(value));
    }

    public Integer getInteger(String key)
    {
        if (hasProperty(key))
        {
            return new Integer(getProperty(key));
        }
        return null;
    }

    public void setInteger(String key, Integer value)
    {
        if (value != null)
        {
            setProperty(key, value.toString());
        }
        else
        {
            setProperty(key, null);
        }
    }
}
