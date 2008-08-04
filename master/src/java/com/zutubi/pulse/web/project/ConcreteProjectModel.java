package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.util.UnaryProcedure;

import java.util.List;

/**
 */
public class ConcreteProjectModel extends ProjectModel
{
    private Project project;
    private List<BuildResult> latestBuilds;
    private int buildRows;

    public ConcreteProjectModel(ProjectsModel group, Project project, List<BuildResult> latestBuilds, int buildRows)
    {
        super(group, project.getName());
        this.project = project;
        this.latestBuilds = latestBuilds;
        this.buildRows = buildRows;
    }

    public Project getProject()
    {
        return project;
    }

    public boolean isConcrete()
    {
        return true;
    }

    public boolean isLeaf()
    {
        return true;
    }

    public boolean isBuilt()
    {
        return latestBuilds.size() > 0;
    }

    public int getBuildRows()
    {
        int buildCount = latestBuilds.size();
        if(buildCount <= 1)
        {
            return 1;
        }
        else if(buildCount > buildRows)
        {
            return buildRows;
        }
        else
        {
            return buildCount;
        }
    }

    public List<BuildResult> getBuilds()
    {
        return latestBuilds.subList(0, getBuildRows());
    }

    public ProjectHealth getHealth()
    {
        return ProjectHealth.fromLatestBuilds(latestBuilds);
    }

    public ResultState getLatestState()
    {
        if(latestBuilds.size() == 0)
        {
            return null;
        }
        else
        {
            return latestBuilds.get(0).getState();
        }
    }

    public int getCount(ProjectHealth health)
    {
        return getHealth() == health ? 1 : 0;
    }

    public int getCount(ResultState state)
    {
        return state == getLatestState() ? 1 : 0;
    }

    public void forEach(UnaryProcedure<ProjectModel> proc)
    {
        proc.process(this);
    }
}
