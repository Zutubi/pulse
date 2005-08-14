package com.cinnamonbob.core;

import java.util.List;
import java.util.LinkedList;

import com.cinnamonbob.core.BobFile;
import com.cinnamonbob.core.BobFileComponent;
import com.cinnamonbob.core.Reference;

/**
 * 
 *
 */
public class SimpleNestedType implements BobFileComponent, Reference
{
    private BobFile project;
    private String name;
    
    private List<SimpleNestedType> nestedTypes = new LinkedList<SimpleNestedType>();
        
    public void setBobFile(BobFile project)
    {
        this.project = project;
    }

    public BobFile getProject()
    {
        return project;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    public void addNested(SimpleNestedType t)
    {
        nestedTypes.add(t);
    }
    
    public SimpleNestedType createCreateType()
    {
        SimpleNestedType t = new SimpleNestedType();
        nestedTypes.add(t);
        return t;
    }
    
    public SimpleNestedType getNestedType(String name)
    {
        for (SimpleNestedType nestedType: nestedTypes)
        {
            if (nestedType.getName().equals(name))
            {
                return nestedType;
            }
        }
        return null;
    }
}
