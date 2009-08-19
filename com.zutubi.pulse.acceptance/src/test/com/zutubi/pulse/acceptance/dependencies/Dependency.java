package com.zutubi.pulse.acceptance.dependencies;

import static com.zutubi.pulse.master.tove.config.project.DependencyConfiguration.REVISION_LATEST_INTEGRATION;

public class Dependency
{
    private Project project;

    private boolean transitive = true;

    private String stage = null;

    private String revision = REVISION_LATEST_INTEGRATION;

    public Dependency(Project project, boolean transitive, String stage, String revision)
    {
        this(project, transitive, stage);
        this.revision = revision;
    }

    public Dependency(Project project, boolean transitive, String stage)
    {
        this(project, transitive);
        this.stage = stage;
    }

    public Dependency(Project project, boolean transitive)
    {
        this(project);
        this.transitive = transitive;
    }

    public Dependency(Project project)
    {
        this.project = project;
    }

    public Project getProject()
    {
        return project;
    }

    public boolean isTransitive()
    {
        return transitive;
    }

    public void setTransitive(boolean transitive)
    {
        this.transitive = transitive;
    }

    public String getStage()
    {
        return stage;
    }

    public String getRevision()
    {
        return revision;
    }

    public void setRevision(String revision)
    {
        this.revision = revision;
    }
}
