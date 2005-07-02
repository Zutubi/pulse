package com.cinnamonbob.core2.config;

import java.util.List;
import java.util.LinkedList;

/**
 * 
 *
 */
public class SimpleNestedType implements ProjectComponent, Reference
{
    private Project project;
    private String name;
    
    private List<SimpleNestedType> nestedTypes = new LinkedList<SimpleNestedType>();
        
    public void setProject(Project project)
    {
        this.project = project;
    }

    public Project getProject()
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
