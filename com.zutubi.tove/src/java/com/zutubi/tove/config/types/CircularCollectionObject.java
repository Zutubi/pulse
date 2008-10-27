package com.zutubi.tove.config.types;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.AbstractConfiguration;

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
