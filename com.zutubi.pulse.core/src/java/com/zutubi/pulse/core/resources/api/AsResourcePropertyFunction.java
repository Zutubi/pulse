package com.zutubi.pulse.core.resources.api;

import com.google.common.base.Function;
import com.zutubi.pulse.core.engine.api.ResourceProperty;

/**
 * Maps from ResourcePropertyConfiguration to ResourceProperty.
 */
public class AsResourcePropertyFunction implements Function<ResourcePropertyConfiguration, ResourceProperty>
{
    public ResourceProperty apply(ResourcePropertyConfiguration config)
    {
        return config.asResourceProperty();
    }
}
