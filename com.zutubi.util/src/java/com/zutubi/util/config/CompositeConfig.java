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
