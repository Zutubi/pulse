package com.zutubi.pulse.acceptance.dependencies;

import java.util.List;
import java.util.LinkedList;

public class Recipe
{
    private Project project;
    private String name;
    private List<Artifact> artifacts = new LinkedList<Artifact>();

    public Recipe(Project project, String name)
    {
        this.project = project;
        this.name = name;
    }

    public Artifact addArtifact(String artifactName)
    {
        Artifact artifact = new Artifact(artifactName, this);
        this.artifacts.add(artifact);
        return artifact;
    }

    public List<Artifact> addArtifacts(String... artifactNames)
    {
        List<Artifact> artifacts = new LinkedList<Artifact>();
        for (String artifactName : artifactNames)
        {
            artifacts.add(addArtifact(artifactName));
        }
        return artifacts;
    }

    public List<Artifact> getArtifacts()
    {
        return artifacts;
    }

    public String getName()
    {
        return name;
    }

    public Project getProject()
    {
        return project;
    }
}
