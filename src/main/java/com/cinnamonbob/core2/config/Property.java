package com.cinnamonbob.core2.config;


/**
 * 
 *
 */
public class Property implements InitComponent, BobFileComponent
{
    private BobFile project;
    private String name;
    private String value;
    
    public void init()
    {
        project.setProperty(name, value);
    }

    public void setBobFile(BobFile project)
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
