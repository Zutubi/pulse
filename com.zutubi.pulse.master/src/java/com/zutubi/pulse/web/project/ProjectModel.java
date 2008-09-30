package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.util.StringUtils;
import com.zutubi.util.UnaryProcedure;

/**
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

    public ProjectModel getParent()
    {
        return parent;
    }

    protected void setParent(ProjectModel parent)
    {
        this.parent = parent;
    }

    public String getId()
    {
        String groupPrefix = group.isLabelled() ? "grouped." : "ungrouped.";
        return StringUtils.toValidHtmlName(groupPrefix + group.getGroupName() + "." + name);
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

    public abstract boolean isConcrete();

    public abstract boolean isLeaf();
    
    public abstract ProjectHealth getHealth();

    public abstract ResultState getLatestState();

    public abstract int getCount(ProjectHealth health);

    public abstract int getCount(ResultState state);

    public abstract void forEach(UnaryProcedure<ProjectModel> proc);
}
