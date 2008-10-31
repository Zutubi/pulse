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
    private CommandLine commandLine;
    private Map<String, String> switchMap = new TreeMap<String, String>();

    public void setCommandLine(CommandLine commandLine)
    {
        this.commandLine = commandLine;
    }

    public void mapSwitch(String switchChar, String property)
    {
        switchMap.put(property, switchChar);
    }

    public String getProperty(String key)
    {
        if(hasProperty(key))
        {
            return commandLine.getOptionValue(switchMap.get(key));
        }

        return null;
    }

    public void setProperty(String key, String value)
    {
        throw new UnsupportedOperationException("Command line is read only");
    }

    public boolean hasProperty(String key)
    {
        if(switchMap.containsKey(key))
        {
            String switchChar = switchMap.get(key);
            return commandLine.hasOption(switchChar);
        }

        return false;
    }

    public void removeProperty(String key)
    {
        throw new UnsupportedOperationException("Command line is read only");
    }

    public boolean isWriteable()
    {
        return false;
    }
}
