package com.zutubi.pulse.acceptance.dependencies;

import java.util.List;
import java.util.LinkedList;

public class RecipeHelper
{
    private ProjectHelper project;
    private String name;
    private List<ArtifactHelper> artifacts = new LinkedList<ArtifactHelper>();

    public RecipeHelper(ProjectHelper project, String name)
    {
        this.project = project;
        this.name = name;
    }

    public ArtifactHelper addArtifact(String artifactName)
    {
        ArtifactHelper artifact = new ArtifactHelper(artifactName, this);
        this.artifacts.add(artifact);
        return artifact;
    }

    public List<ArtifactHelper> addArtifacts(String... artifactNames)
    {
        List<ArtifactHelper> artifacts = new LinkedList<ArtifactHelper>();
        for (String artifactName : artifactNames)
        {
            artifacts.add(addArtifact(artifactName));
        }
        return artifacts;
    }

    public List<ArtifactHelper> getArtifacts()
    {
        return artifacts;
    }

    public String getName()
    {
        return name;
    }

    public ProjectHelper getProject()
    {
        return project;
    }
}
