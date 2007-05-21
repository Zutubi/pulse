package com.zutubi.pulse.core.config;

import com.zutubi.prototype.Formatter;

/**
 *
 *
 */
public class ResourceVersionFormatter implements Formatter<ResourceVersion>
{
    public String format(ResourceVersion obj)
    {
        return obj.getValue();
    }
}
