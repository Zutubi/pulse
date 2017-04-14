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

import java.util.Properties;

public class PropertiesConfig implements Config
{
    private final Properties props;

    public PropertiesConfig()
    {
        this(null);
    }

    public PropertiesConfig(Properties props)
    {
        if (props == null)
        {
            props = new Properties();
        }
        this.props = props;
    }

    public String getProperty(String key)
    {
        return props.getProperty(key);
    }

    public void setProperty(String key, String value)
    {
        props.setProperty(key, value);
    }

    public boolean hasProperty(String key)
    {
        return props.containsKey(key);
    }

    public void removeProperty(String key)
    {
        props.remove(key);
    }

    public boolean isWritable()
    {
        return true;
    }
}
