package com.zutubi.pulse.bootstrap.conf;

/**
 * <class-comment/>
 */
public class CompositeConfig implements Config
{
    private final Config[] delegates;

    public CompositeConfig(Config... configs)
    {
        this.delegates = configs;
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
        delegates[0].setProperty(key, value);
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
            if (config.hasProperty(key))
            {
                try
                {
                    config.removeProperty(key);
                }
                catch (UnsupportedOperationException e)
                {
                    // noop. read only config.
                }
                return;
            }
        }
    }
}
