package com.zutubi.pulse.core.resources.api;

import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.util.Mapping;

/**
 * Maps from ResourcePropertyConfiguration to ResourceProperty.
 */
public class AsResourcePropertyMapping implements Mapping<ResourcePropertyConfiguration, ResourceProperty>
{
    public ResourceProperty map(ResourcePropertyConfiguration config)
    {
        return config.asResourceProperty();
    }
}
