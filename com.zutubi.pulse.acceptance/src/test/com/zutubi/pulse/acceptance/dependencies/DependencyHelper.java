package com.zutubi.pulse.acceptance.dependencies;

import static com.zutubi.pulse.master.tove.config.project.DependencyConfiguration.REVISION_LATEST_INTEGRATION;

public class DependencyHelper
{
    private ProjectHelper project;

    private boolean transitive = true;

    private String stage = null;

    private String revision = REVISION_LATEST_INTEGRATION;

    public DependencyHelper(ProjectHelper project, boolean transitive, String stage, String revision)
    {
        this(project, transitive, stage);
        this.revision = revision;
    }

    public DependencyHelper(ProjectHelper project, boolean transitive, String stage)
    {
        this(project, transitive);
        this.stage = stage;
    }

    public DependencyHelper(ProjectHelper project, boolean transitive)
    {
        this(project);
        this.transitive = transitive;
    }

    public DependencyHelper(ProjectHelper project)
    {
        this.project = project;
    }

    public ProjectHelper getProject()
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
