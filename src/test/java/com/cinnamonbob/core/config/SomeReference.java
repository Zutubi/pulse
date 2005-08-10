package com.cinnamonbob.core.config;

import com.cinnamonbob.core.config.BobFile;
import com.cinnamonbob.core.config.BobFileComponent;
import com.cinnamonbob.core.config.Reference;

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
