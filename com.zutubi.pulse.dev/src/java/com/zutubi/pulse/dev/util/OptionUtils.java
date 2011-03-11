package com.zutubi.pulse.dev.util;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.util.StringUtils;
import org.apache.commons.cli.ParseException;

import java.util.Properties;

/**
 * Utilities shared by option parsing for different commands.
 */
public class OptionUtils
{
    /**
     * Parses a resource requirement.  Requirements are must have at least a
     * resource name, optionally followed by a forward slash and version.
     * 
     * @param arg the argument to parse
     * @return a resource requirement parse from the argument
     * @throws PulseException if the format of the argument is invalid
     */
    public static ResourceRequirement parseResourceRequirement(String arg) throws PulseException
    {
        if (!StringUtils.stringSet(arg))
        {
            throw new PulseException("Resource requirement is empty");
        }
        
        int slashOffset = arg.indexOf('/');
        if (slashOffset < 0)
        {
            return new ResourceRequirement(arg, false);
        }
        else if (slashOffset == arg.length() - 1)
        {
            return new ResourceRequirement(arg.substring(0, slashOffset), false);
        }
        else
        {
            String name = arg.substring(0, slashOffset);
            if (!StringUtils.stringSet(name))
            {
                throw new PulseException("Resource requirement '" + arg + "' has empty resource name");
            }

            return new ResourceRequirement(name, arg.substring(slashOffset + 1), false);
        }
    }

    /**
     * Parses a property definition from the given argument and adds it to the
     * given properties.  The property should have the form name=value.
     * 
     * @param arg        the argument to parse
     * @param properties properties to add the parsed definition to
     * @throws ParseException if the argument format is invalid
     */
    public static void addDefinedOption(String arg, Properties properties) throws ParseException
    {
        int index = arg.indexOf('=');
        if (index <= 0 || index >= arg.length() - 1)
        {
            throw new ParseException("Invalid property definition syntax '" + arg + "' (expected name=value)");
        }

        String propertyName = arg.substring(0, index);
        String propertyValue = arg.substring(index + 1);

        properties.put(propertyName, propertyValue);
    }
}
