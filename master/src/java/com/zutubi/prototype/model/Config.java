package com.zutubi.prototype.model;

import com.zutubi.pulse.prototype.record.RecordTypeInfo;

import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class Config
{
    private List<String> valueListProperties = new LinkedList<String>();
    private List<String> simpleProperties = new LinkedList<String>();
    private List<String> nestedProperties = new LinkedList<String>();
    private List<String> extensions = new LinkedList<String>();

    public void addValueListProperty(String s)
    {
        valueListProperties.add(s);
    }

    public void addNestedProperty(String s)
    {
        nestedProperties.add(s);
    }

    public void addSimpleProperty(String s)
    {
        simpleProperties.add(s);
    }

    public List<String> getValueListProperties()
    {
        return valueListProperties;
    }

    public List<String> getSimpleProperties()
    {
        return simpleProperties;
    }

    public List<String> getNestedProperties()
    {
        return nestedProperties;
    }

    public boolean hasSimpleProperties()
    {
        return simpleProperties.size() > 0;
    }

    public void addExtension(String extension)
    {
        extensions.add(extension);
    }

    public List<String> getExtensions()
    {
        return extensions;
    }

    public boolean hasExtensions()
    {
        return extensions.size() > 0;
    }
}
