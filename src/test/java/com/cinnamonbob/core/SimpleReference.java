package com.cinnamonbob.core;

import com.cinnamonbob.core.BobFile;
import com.cinnamonbob.core.BobFileComponent;
import com.cinnamonbob.core.Reference;

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
