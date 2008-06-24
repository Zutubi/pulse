package com.zutubi.prototype.config.docs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds documentation for a single type in a form that can be rendered for
 * various UIs.
 */
public class TypeDocs implements Docs
{
    private String symbolicName;
    private String title;
    private String brief;
    private String verbose;
    private Map<String, PropertyDocs> properties = new HashMap<String, PropertyDocs>();

    public TypeDocs(String symbolicName)
    {
        this.symbolicName = symbolicName;
    }

    public String getSymbolicName()
    {
        return symbolicName;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getBrief()
    {
        return brief;
    }

    public void setBrief(String brief)
    {
        this.brief = brief;
    }

    public String getVerbose()
    {
        return verbose;
    }

    public void setVerbose(String verbose)
    {
        this.verbose = verbose;
    }

    public Map<String, PropertyDocs> getProperties()
    {
        return Collections.unmodifiableMap(properties);
    }

    public PropertyDocs getPropertyDocs(String fieldName)
    {
        return properties.get(fieldName);
    }

    public void addProperty(PropertyDocs property)
    {
        properties.put(property.getName(), property);
    }
}
