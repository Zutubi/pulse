package com.cinnamonbob.core2.config;


/**
 * 
 *
 */
public class Property implements InitComponent, ProjectComponent
{
    private Project project;
    private String name;
    private String value;
    
    public void init()
    {
        project.setProperty(name, value);
    }

    public void setProject(Project project)
    {
        this.project = project;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
}
