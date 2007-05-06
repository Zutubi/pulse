package com.zutubi.pulse.model;

import com.zutubi.prototype.Formatter;

/**
 *
 *
 */
public class ResourceRequirementFormatter implements Formatter<ResourceRequirement>
{
    public String format(ResourceRequirement obj)
    {
        return obj.getResource() + " " + obj.getVersion();
    }
}
