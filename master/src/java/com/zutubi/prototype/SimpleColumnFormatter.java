package com.zutubi.prototype;

import com.zutubi.pulse.core.config.NamedConfiguration;

/**
 *
 *
 */
public class SimpleColumnFormatter implements Formatter<Object>
{
    public String format(Object obj)
    {
        if (obj == null)
        {
            return "";
        }
        if (obj instanceof NamedConfiguration)
        {
            return ((NamedConfiguration)obj).getName();
        }
        
        return obj.toString();
    }
}
