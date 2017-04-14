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

package com.zutubi.pulse.master.tove.config.project.reports;

import com.zutubi.util.StringUtils;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.FieldValidatorSupport;

import java.awt.*;
import java.lang.reflect.Field;

/**
 * Validates custom colour values for report series.
 */
public class ColourValidator extends FieldValidatorSupport
{
    protected void validateField(Object value) throws ValidationException
    {
        if (value instanceof String)
        {
            String s = (String) value;
            if (StringUtils.stringSet(s))
            {
                try
                {
                    parseColour(s);
                }
                catch (IllegalArgumentException e)
                {
                    throw new ValidationException(e.getMessage());
                }
            }
        }
    }

    /**
     * Parses a string to a colour instance.  The string can be either a
     * numerical RGB value (hex is easiest, e.g. 0xffbb80), or a constant name
     * from {@link java.awt.Color}.
     *
     * @param colour the colour string to parse
     * @return a corresponing Color instance
     * @throws IllegalArgumentException if the string cannot be resolved to a
     *         Color
     */
    public static Color parseColour(final String colour) throws IllegalArgumentException
    {
        // Adapted from code in jcommon (part of JFreeChart).
        try
        {
            // Get colour by hex or octal value
            return Color.decode(colour);
        }
        catch (NumberFormatException nfe)
        {
            try
            {
                // Try to get a color by name using reflection
                // black is used for an instance and not for the color itself
                final Field f = Color.class.getField(colour);
                return (Color) f.get(null);
            }
            catch (Exception ce)
            {
                throw new IllegalArgumentException("Unrecognised colour '" + colour + "'");
            }
        }
    }
}
