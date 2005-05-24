package com.cinnamonbob.core2.type;

import com.cinnamonbob.core2.Project;

/**
 * 
 *
 */
public abstract class AbstractType implements Type
{

    private Project project = null;
    private String id;
    
    public void setProject(Project p)
    {
        this.project = p;
    }

    public Project getProject()
    {
        return this.project;
    }
    
    public void setId(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return this.id;
    }

}
