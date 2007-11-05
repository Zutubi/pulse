package com.zutubi.pulse.core;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class SimpleNestedType extends SelfReference
{
    private List<SimpleNestedType> nestedTypes = new LinkedList<SimpleNestedType>();
        
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
