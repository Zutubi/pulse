package com.zutubi.prototype.config.types;

import com.zutubi.pulse.prototype.record.SymbolicName;
import com.zutubi.prototype.config.types.SimpleObject;

import java.util.List;

/**
 */
@SymbolicName("SimpleCollection")
public class SimpleCollectionObject
{
    List<SimpleObject> simpleList;

    public List<SimpleObject> getSimpleList()
    {
        return simpleList;
    }

    public void setSimpleList(List<SimpleObject> simpleList)
    {
        this.simpleList = simpleList;
    }
}
