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

        boolean inverse = false;
        if (arg.startsWith("!"))
        {
            arg = arg.substring(1);
            inverse = true;
        }

        int slashOffset = arg.indexOf('/');
        if (slashOffset < 0)
        {
            return new ResourceRequirement(arg, inverse, false);
        }
        else if (slashOffset == arg.length() - 1)
        {
            return new ResourceRequirement(arg.substring(0, slashOffset), inverse, false);
        }
        else
        {
            String name = arg.substring(0, slashOffset);
            if (!StringUtils.stringSet(name))
            {
                throw new PulseException("Resource requirement '" + arg + "' has empty resource name");
            }

            return new ResourceRequirement(name, arg.substring(slashOffset + 1), inverse, false);
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
