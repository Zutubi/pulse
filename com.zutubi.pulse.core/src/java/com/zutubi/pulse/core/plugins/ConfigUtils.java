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

package com.zutubi.pulse.core.plugins;

import com.zutubi.util.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Utility classes for dealing with the configuration stored in plugin.xml
 * files.
 */
public class ConfigUtils
{
    public static String getString(IConfigurationElement config, String attribute, String defaultValue)
    {
        String value = config.getAttribute(attribute);
        if(StringUtils.stringSet(value))
        {
            return value;
        }
        else
        {
            return defaultValue;
        }
    }

    public static boolean getBoolean(IConfigurationElement config, String attribute, boolean defaultValue)
    {
        String value = config.getAttribute(attribute);
        if(StringUtils.stringSet(value))
        {
            return Boolean.parseBoolean(value);
        }
        else
        {
            return defaultValue;
        }
    }
}
