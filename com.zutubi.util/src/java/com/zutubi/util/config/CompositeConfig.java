package com.zutubi.util.config;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A chain of config objects.  When looking up by key, the first config in
 * the chain is asked first.  If the key is not found, the next config is
 * asked and so on until the key is found or the end of the chain is reached.
 */
public class CompositeConfig implements Config
{
    private List<Config> delegates;

    public CompositeConfig(Config... configs)
    {
        delegates = new LinkedList<Config>(Arrays.asList(configs));
    }

    public void append(Config config)
    {
        delegates.add(config);
    }

    public void prepend(Config config)
    {
        delegates.add(0, config);
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
            if (!config.isWritable())
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
            if (!config.isWritable())
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
    public boolean isWritable()
    {
        if (delegates.size() > 0)
        {
            return delegates.get(0).isWritable();
        }
        return false;
    }
}
