package com.cinnamonbob.core;

import com.cinnamonbob.core.BobFile;
import com.cinnamonbob.core.BobFileComponent;

/**
 * 
 *
 */
public class SimpleType implements BobFileComponent
{
    private BobFile project;
    private String name;
    private String value;
    
    public BobFile getProject()
    {
        return project;
    }

    public void setBobFile(BobFile project)
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
