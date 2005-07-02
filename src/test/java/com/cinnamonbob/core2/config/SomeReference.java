package com.cinnamonbob.core2.config;

/**
 * 
 *
 */
public class SomeReference implements ProjectComponent, Reference
{
    private String name;
    private Project project;
    private String someValue;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Project getProject()
    {
        return project;
    }

    public void setProject(Project project)
    {
        this.project = project;
    }

    public String getSomeValue()
    {
        return someValue;
    }

    public void setSomeValue(String someValue)
    {
        this.someValue = someValue;
    }
}
