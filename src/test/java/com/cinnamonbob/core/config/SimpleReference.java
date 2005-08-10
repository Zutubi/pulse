package com.cinnamonbob.core.config;

import com.cinnamonbob.core.config.BobFile;
import com.cinnamonbob.core.config.BobFileComponent;
import com.cinnamonbob.core.config.Reference;

/**
 * 
 *
 */
public class SimpleReference implements BobFileComponent, Reference
{
    private String name;
    private BobFile project;
    private Reference ref;
    
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

    public Reference getRef()
    {
        return ref;
    }

    public void setRef(Reference ref)
    {
        this.ref = ref;
    }
}
