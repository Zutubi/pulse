package com.zutubi.prototype.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Collections;

/**
 *
 *
 */
public class Config
{
    private List<String> listProperties = new LinkedList<String>();
    private List<String> simpleProperties = new LinkedList<String>();
    private List<String> nestedProperties = new LinkedList<String>();
    private List<String> mapProperties = new LinkedList<String>();

    public void addListProperty(String propertyName)
    {
        listProperties.add(propertyName);
    }

    public List<String> getListProperties()
    {
        return Collections.unmodifiableList(listProperties);
    }

    public boolean hasListProperties()
    {
        return listProperties.size() > 0;
    }

    public void addSimpleProperty(String propertyName)
    {
        simpleProperties.add(propertyName);
    }

    public List<String> getSimpleProperties()
    {
        return Collections.unmodifiableList(simpleProperties);
    }

    public boolean hasSimpleProperties()
    {
        return simpleProperties.size() > 0;
    }

    public void addNestedProperty(String propertyName)
    {
        nestedProperties.add(propertyName);
    }

    public List<String> getNestedProperties()
    {
        return Collections.unmodifiableList(nestedProperties);
    }

    public boolean hasNestedProperties()
    {
        return nestedProperties.size() > 0;
    }

    public void addMapProperty(String propertyName)
    {
        mapProperties.add(propertyName);
    }

    public List<String> getMapProperties()
    {
        return Collections.unmodifiableList(mapProperties);
    }
}
