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
    public static final int DEFAULT_WORK_DIR_DAYS = 30;

    private String name;
    private String description;
    private BobFileDetails bobFileDetails;
    private AgeBuildResultCleanupPolicy cleanupPolicy = new AgeBuildResultCleanupPolicy(DEFAULT_WORK_DIR_DAYS, AgeBuildResultCleanupPolicy.NEVER_CLEAN);
    private Scm scm;

    private List<BuildSpecification> buildSpecifications;

    public Project()
    {
    }

    public Project(String name, String description)
    {
        this.name = name;
        this.description = description;
        this.bobFileDetails = new CustomBobFileDetails("bob.xml");
    }

    public Project(String name, String description, BobFileDetails bobFileDetails)
    {
        this.name = name;
        this.description = description;
        this.bobFileDetails = bobFileDetails;
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

    public AgeBuildResultCleanupPolicy getCleanupPolicy()
    {
        return cleanupPolicy;
    }

    public void setCleanupPolicy(AgeBuildResultCleanupPolicy cleanupPolicy)
    {
        this.cleanupPolicy = cleanupPolicy;
    }

    public Scm getScm()
    {
        return scm;
    }

    public void setScm(Scm scm)
    {
        this.scm = scm;
    }

    public List<BuildSpecification> getBuildSpecifications()
    {
        if (buildSpecifications == null)
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

    public BobFileDetails getBobFileDetails()
    {
        return bobFileDetails;
    }

    public void setBobFileDetails(BobFileDetails bobFileDetails)
    {
        this.bobFileDetails = bobFileDetails;
    }
}
