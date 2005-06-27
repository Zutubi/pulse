package com.cinnamonbob.core.config;

/**
 * 
 *
 */
public class SimpleType implements ProjectComponent
{
    private Project project;
    private String name;
    private String value;
    
    public Project getProject()
    {
        return project;
    }

    public void setProject(Project project)
    {
        this.project = project;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
}
