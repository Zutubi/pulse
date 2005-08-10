package com.cinnamonbob.core.config;

import java.util.List;
import java.util.LinkedList;

import com.cinnamonbob.core.config.BobFile;
import com.cinnamonbob.core.config.BobFileComponent;
import com.cinnamonbob.core.config.Reference;

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
