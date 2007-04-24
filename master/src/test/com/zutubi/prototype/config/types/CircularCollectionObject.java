package com.zutubi.prototype.config.types;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.config.types.CircularObject;
import com.zutubi.pulse.core.config.AbstractConfiguration;

import java.util.List;

/**
 */
@SymbolicName("CircularCollection")
public class CircularCollectionObject extends AbstractConfiguration
{
    List<CircularObject> circularList;

    public List<CircularObject> getCircularList()
    {
        return circularList;
    }

    public void setCircularList(List<CircularObject> circularList)
    {
        this.circularList = circularList;
    }
}
