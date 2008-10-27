package com.zutubi.tove.config.types;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.AbstractConfiguration;

import java.util.List;

/**
 */
@SymbolicName("SimpleCollection")
public class SimpleCollectionObject extends AbstractConfiguration
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
