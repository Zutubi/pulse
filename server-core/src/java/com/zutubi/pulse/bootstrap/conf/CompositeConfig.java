package com.zutubi.pulse.bootstrap.conf;

/**
 * <class-comment/>
 */
public class CompositeConfig implements Config
{
    private final Config[] delegates;

    public CompositeConfig(Config... configs)
    {
        this.delegates = (configs != null ? configs : new Config[0]);
    }

    public String getProperty(String key)
    {
        for (Config config: delegates)
        {
            if (config.hasProperty(key))
            {
                return config.getProperty(key);
            }
        }
        return null;
    }

    public void setProperty(String key, String value)
    {
        for (Config config: delegates)
        {
            if (!config.isWriteable())
            {
                return;
            }
            config.setProperty(key, value);
        }
    }

    public boolean hasProperty(String key)
    {
        for (Config config: delegates)
        {
            if (config.hasProperty(key))
            {
                return true;
            }
        }
        return false;
    }

    public void removeProperty(String key)
    {
        for (Config config: delegates)
        {
            if (!config.isWriteable())
            {
                return;
            }
            if (config.hasProperty(key))
            {
                config.removeProperty(key);
            }
        }
    }

    /**
     * If the first delegate is writable, this composite is also considered writable.
     *
     * @return true if this composite is writable.
     */
    public boolean isWriteable()
    {
        if (delegates.length > 0)
        {
            return delegates[0].isWriteable();
        }
        return false;
    }
}
