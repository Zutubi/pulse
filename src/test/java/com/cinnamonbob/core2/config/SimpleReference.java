package com.cinnamonbob.core2.config;

/**
 * 
 *
 */
public class SimpleReference implements ProjectComponent, Reference
{
    private String name;
    private Project project;
    private Reference ref;
    
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Project getProject()
    {
        return project;
    }

    public void setProject(Project project)
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
