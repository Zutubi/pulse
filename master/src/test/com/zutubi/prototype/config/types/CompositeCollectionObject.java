package com.zutubi.prototype.config.types;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.config.types.CompositeObject;

import java.util.Map;

/**
 */
@SymbolicName("CompositeCollection")
public class CompositeCollectionObject
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
