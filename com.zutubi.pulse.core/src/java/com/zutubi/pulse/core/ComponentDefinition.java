package com.zutubi.pulse.core;

/**
 * Used internally by the ProjectConfigurationLoader to dynamically
 * add new definitions to the system. These extra definitions are 
 * only available in the scope of the project in which they are defined.
 *
 */
public class ComponentDefinition
{
    private String name;
    private Class clz;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Class getClazz()
    {
        return clz;
    }

    public void setClass(Class clz)
    {
        this.clz = clz;
    }
    
}
