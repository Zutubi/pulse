package com.cinnamonbob.core;

import com.cinnamonbob.core.BobFile;
import com.cinnamonbob.core.BobFileComponent;
import com.cinnamonbob.core.Reference;

/**
 * 
 *
 */
public class SimpleReference implements Reference
{
    private String name;
    private Reference ref;
    
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

    public Reference getRef()
    {
        return ref;
    }

    public void setRef(Reference ref)
    {
        this.ref = ref;
    }
}
