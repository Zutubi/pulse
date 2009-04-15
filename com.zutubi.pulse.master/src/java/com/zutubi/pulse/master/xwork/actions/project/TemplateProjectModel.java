package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.Sort;

import java.util.LinkedList;
import java.util.List;
import java.util.Comparator;
import java.util.Collections;

/**
 */
public class TemplateProjectModel extends ProjectModel
{
    private static final ProjectHealth[] SUMMARY_HEALTHS = { ProjectHealth.OK, ProjectHealth.WARNINGS, ProjectHealth.BROKEN };
    private static final ChildComparator CHILD_COMPARATOR = new ChildComparator();
    private List<ProjectModel> children = new LinkedList<ProjectModel>();

    public TemplateProjectModel(ProjectsModel group, String name)
    {
        super(group, name);
    }

    public List<ProjectModel> getChildren()
    {
        return children;
    }

    public void addChild(ProjectModel child)
    {
        children.add(child);
        child.setParent(this);
        Collections.sort(children, CHILD_COMPARATOR);
    }

    public ProjectHealth[] getSummaryHealths()
    {
        return SUMMARY_HEALTHS;
    }

    public boolean isConcrete()
    {
        return false;
    }

    public boolean isLeaf()
    {
        return children.size() == 0;
    }

    public ProjectHealth getHealth()
    {
        ProjectHealth health = ProjectHealth.UNKNOWN;
        for (ProjectModel child: children)
        {
            ProjectHealth childHealth = child.getHealth();
            if(childHealth.ordinal() > health.ordinal())
            {
                health = childHealth;
            }
        }

        return health;
    }

    public ResultState getLatestState()
    {
        return null;
    }

    public int getCount(ProjectHealth health)
    {
        int count = 0;
        for(ProjectModel child: children)
        {
            count += child.getCount(health);
        }

        return count;
    }

    public int getCount(final ResultState state)
    {
        int count = 0;
        for(ProjectModel child: children)
        {
            count += child.getCount(state);
        }

        return count;
    }

    public void forEach(UnaryProcedure<ProjectModel> proc)
    {
        proc.process(this);
        for(ProjectModel child: children)
        {
            child.forEach(proc);
        }
    }

    /**
     * A comparator that sorts the template project models to the end of the list, and in
     * lexological order.
     */
    private static class ChildComparator implements Comparator<ProjectModel>
    {
        private Comparator<String> stringComparator = new Sort.StringComparator();

        public int compare(ProjectModel o1, ProjectModel o2)
        {
            if (o1 instanceof TemplateProjectModel && o2 instanceof TemplateProjectModel)
            {
                return stringComparator.compare(o1.getName(), o2.getName());
            }
            if (o1 instanceof TemplateProjectModel)
            {
                return 1;
            }
            if (o2 instanceof TemplateProjectModel)
            {
                return -1;
            }

            return 0;
        }
    }
}
