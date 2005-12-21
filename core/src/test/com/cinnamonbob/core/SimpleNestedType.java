package com.cinnamonbob.core;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class SimpleNestedType implements Reference
{
    private String name;
    
    private List<SimpleNestedType> nestedTypes = new LinkedList<SimpleNestedType>();
        
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
