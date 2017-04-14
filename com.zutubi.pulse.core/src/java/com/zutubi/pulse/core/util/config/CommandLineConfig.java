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

package com.zutubi.pulse.core.util.config;

import com.zutubi.util.config.Config;
import org.apache.commons.cli.CommandLine;

import java.util.Map;
import java.util.TreeMap;

/**
 * A utility that maps command line arguments to a configuration object.
 */
public class CommandLineConfig implements Config
{
    public static final String INVERT_PREFIX = "no-";

    private CommandLine commandLine;
    private Map<String, String> switchMap = new TreeMap<String, String>();
    private Map<String, String> booleanMap = new TreeMap<String, String>();

    public void setCommandLine(CommandLine commandLine)
    {
        this.commandLine = commandLine;
    }

    public void mapSwitch(String switchChar, String property)
    {
        switchMap.put(property, switchChar);
    }

    public void mapBoolean(String option, String property)
    {
        booleanMap.put(property, option);
    }

    public String getProperty(String key)
    {
        if (hasSwitchProperty(key))
        {
            return commandLine.getOptionValue(switchMap.get(key));
        }

        if (hasBooleanProperty(key))
        {
            String option = booleanMap.get(key);
            if (commandLine.hasOption(option))
            {
                return Boolean.TRUE.toString();
            }

            if (commandLine.hasOption(INVERT_PREFIX + option))
            {
                return Boolean.FALSE.toString();
            }
        }

        return null;
    }

    public void setProperty(String key, String value)
    {
        throw new UnsupportedOperationException("Command line is read only");
    }

    public boolean hasProperty(String key)
    {
        return hasSwitchProperty(key) || hasBooleanProperty(key);
    }

    private boolean hasSwitchProperty(String key)
    {
        if (switchMap.containsKey(key))
        {
            String switchChar = switchMap.get(key);
            return commandLine.hasOption(switchChar);
        }

        return false;
    }

    private boolean hasBooleanProperty(String key)
    {
        if (booleanMap.containsKey(key))
        {
            String option = booleanMap.get(key);
            return commandLine.hasOption(option) || commandLine.hasOption(INVERT_PREFIX + option);
        }

        return false;
    }

    public void removeProperty(String key)
    {
        throw new UnsupportedOperationException("Command line is read only");
    }

    public boolean isWritable()
    {
        return false;
    }
}
