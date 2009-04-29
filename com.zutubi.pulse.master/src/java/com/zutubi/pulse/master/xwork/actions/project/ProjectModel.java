package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.util.StringUtils;
import com.zutubi.util.UnaryProcedure;

/**
 * JSON-encodable details for a single project or project template for display
 * on the dashboard or browse view.
 */
public abstract class ProjectModel
{
    private ProjectsModel group;
    private String name;
    private ProjectModel parent;

    protected ProjectModel(ProjectsModel group, String name)
    {
        this.group = group;
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public String getId()
    {
        String groupPrefix = group.isLabelled() ? "grouped." + group.getGroupName() : "ungrouped";
        return StringUtils.toValidHtmlName(groupPrefix + "." + name);
    }

    public String getHealth()
    {
        return latestHealth().toString().toLowerCase();
    }

    public int getDepth()
    {
        if (parent == null)
        {
            return 0;
        }
        else
        {
            return parent.getDepth() + 1;
        }
    }

    public int getInProgressCount()
    {
        return getCount(ResultState.IN_PROGRESS);
    }

    protected void setParent(ProjectModel parent)
    {
        this.parent = parent;
    }

    public abstract boolean isConcrete();

    public abstract ProjectHealth latestHealth();

    public abstract ResultState latestState();

    public abstract int getCount(ProjectHealth health);

    public abstract int getCount(ResultState state);

    public abstract void forEach(UnaryProcedure<ProjectModel> proc);
}
