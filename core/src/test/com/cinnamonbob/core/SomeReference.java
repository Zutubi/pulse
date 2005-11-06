package com.cinnamonbob.core;

import com.cinnamonbob.core.BobFile;
import com.cinnamonbob.core.BobFileComponent;
import com.cinnamonbob.core.Reference;

/**
 * 
 *
 */
public class SomeReference implements BobFileComponent, Reference
{
    private String name;
    private BobFile project;
    private String someValue;

    public String getName()
    {
        return name;
    }

    public Object getValue()
    {
        return this;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public BobFile getProject()
    {
        return project;
    }

    public void setBobFile(BobFile project)
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
