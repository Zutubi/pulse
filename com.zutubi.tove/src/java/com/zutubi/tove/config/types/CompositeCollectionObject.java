package com.zutubi.tove.config.types;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.tove.config.AbstractConfiguration;

import java.util.Map;

/**
 */
@SymbolicName("CompositeCollection")
public class CompositeCollectionObject extends AbstractConfiguration
{
    private Map<String, CompositeObject> composites;

    public Map<String, CompositeObject> getComposites()
    {
        return composites;
    }

    public void setComposites(Map<String, CompositeObject> composites)
    {
        this.composites = composites;
    }
}
