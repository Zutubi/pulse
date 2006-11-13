package com.zutubi.pulse.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class CompositeConfig implements Config
{
    private List<Config> delegates;

    public CompositeConfig(Config... configs)
    {
        if(configs == null)
        {
            delegates = new ArrayList<Config>();
        }
        else
        {
            delegates = new LinkedList<Config>(Arrays.asList(configs));
        }
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
        if (delegates.size() > 0)
        {
            return delegates.get(0).isWriteable();
        }
        return false;
    }
}
