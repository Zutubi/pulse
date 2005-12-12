package com.cinnamonbob.model;

import com.cinnamonbob.core.model.Entity;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class Project extends Entity
{
    private String name;
    private String description;
    private String bobFile;
    private List<Scm> scms;

    private List<BuildSpecification> buildSpecifications;

    public Project()
    {
        bobFile = "bob.xml";
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

    public List<Scm> getScms()
    {
        if(scms == null)
        {
            scms = new LinkedList<Scm>();
        }
        
        return scms;
    }

    public void addScm(Scm scm)
    {
        getScms().add(scm);
        scm.setProject(this);
    }

    private void setScms(List<Scm> scms)
    {
        this.scms = scms;
    }

    public Scm getScm(String name)
    {
        for (Scm scm : scms)
        {
            if (scm.getName().compareToIgnoreCase(name) == 0)
            {
                return scm;
            }
        }
        return null;
    }

    public boolean remove(Scm scm)
    {
        if (scms.remove(scm))
        {
            scm.setProject(null);
            return true;
        }
        return false;
    }

    public List<BuildSpecification> getBuildSpecifications()
    {
        if(buildSpecifications == null)
        {
            buildSpecifications = new LinkedList<BuildSpecification>();
        }

        return buildSpecifications;
    }

    public void addBuildSpecification(BuildSpecification specification)
    {
        getBuildSpecifications().add(specification);
    }

    private void setBuildSpecifications(List<BuildSpecification> buildSpecifications)
    {
        this.buildSpecifications = buildSpecifications;
    }

    public BuildSpecification getBuildSpecification(String name)
    {
        for (BuildSpecification spec : buildSpecifications)
        {
            if (spec.getName().compareToIgnoreCase(name) == 0)
            {
                return spec;
            }
        }
        return null;
    }

    public boolean remove(BuildSpecification buildSpecification)
    {
        return buildSpecifications.remove(buildSpecification);
    }

    public String getBobFile()
    {
        return bobFile;
    }

    public void setBobFile(String bobFile)
    {
        this.bobFile = bobFile;
    }
}
