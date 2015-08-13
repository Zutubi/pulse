package com.zutubi.pulse.master.tove.config;

import com.zutubi.pulse.master.tove.handler.MapOptionProvider;
import com.zutubi.tove.type.EnumType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.util.EnumUtils;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An option provider for enums.  By default, all enum values are listed,
 * with a "nice" conversion for typical UPPER_CASE names.
 */
public class EnumOptionProvider extends MapOptionProvider
{
    public Option getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        return new Option("", "");
    }

    public Map<String,String> getMap(Object instance, String path, TypeProperty property)
    {
        EnumType enumType = (EnumType) property.getType().getTargetType();
        Class<? extends Enum> enumClass = enumType.getClazz();
        Map<String, String> options = new LinkedHashMap<String, String>();

        EnumSet<? extends Enum> allValues = EnumSet.allOf(enumClass);
        for(Enum e: allValues)
        {
            if (includeOption(e))
            {
                options.put(e.toString(), getPrettyName(e));
            }
        }
        
        return options;
    }

    /**
     * Returns true if the given value should be included.  Allows subclasses
     * to filter available values.
     *
     * @param e the value to test
     * @return true for all options by default
     */
    protected boolean includeOption(Enum e)
    {
        return true;
    }

    /**
     * Returns a pretty name for the given value, to display to the user.
     * By default, this is the enum name converted to lower case and with
     * spaces in place of underscores.
     *
     * @param e value to get the pretty name for
     * @return a user-firendly name for the value
     */
    public static String getPrettyName(Enum e)
    {
        return EnumUtils.toPrettyString(e);
    }
}
