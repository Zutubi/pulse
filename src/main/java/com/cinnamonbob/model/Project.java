package com.cinnamonbob.model;

import java.util.List;
import java.util.LinkedList;

/**
 * 
 *
 */
public class Project extends Entity
{
    private String name;
    private String description;
    private List scms;

    public Project()
    {

    }

    public Project(String name, String description)
    {
        this.name = name;
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public List getScms()
    {
        if(scms == null)
        {
            scms = new LinkedList();
        }
        
        return scms;
    }

    public void addScm(Scm scm)
    {
        getScms().add(scm);
        scm.setProject(this);
    }

    private void setScms(List scms)
    {
        this.scms = scms;
    }
}
