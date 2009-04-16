package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.util.Sort;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The projects model sorter is used to handle sorting a list of ProjectsModel
 * instances.  It supports the configuration of the comparator for sorting
 * groups and projects.  By default, both of these comparators use lexical
 * sorting.
 */
public class ProjectsModelSorter
{
    private Comparator<String> groupNameComparator = new Sort.StringComparator();
    private Comparator<String> projectNameComparator = new Sort.StringComparator();

    public void setGroupNameComparator(Comparator<String> groupNameComparator)
    {
        this.groupNameComparator = groupNameComparator;
    }

    public void setProjectNameComparator(Comparator<String> projectNameComparator)
    {
        this.projectNameComparator = projectNameComparator;
    }

    public void sort(List<ProjectsModel> projectsModels)
    {
        // a) sort the groups.
        Collections.sort(projectsModels, new Comparator<ProjectsModel>()
        {
            public int compare(ProjectsModel o1, ProjectsModel o2)
            {
                if (!o1.isLabelled())
                {
                     // ensure unlabelled groups end up at the end.
                    return 1;
                }
                if (!o2.isLabelled())
                {
                    // ensure unlabelled groups end up at the end.
                    return -1;
                }

                return groupNameComparator.compare(o1.getGroupName(), o2.getGroupName());
            }
        });
        
        final Comparator<ProjectModel> projectModelComparator = new ProjectModelComparator(projectNameComparator);
        
        // b) sort the contents of the group.
        for (ProjectsModel projects : projectsModels)
        {
            sort(projects.getRoot(), projectModelComparator);
        }
    }

    private void sort(TemplateProjectModel root, Comparator<ProjectModel> projectModelComparator)
    {
        for (ProjectModel project : root.getChildren())
        {
            if (project instanceof TemplateProjectModel)
            {
                sort((TemplateProjectModel) project, projectModelComparator);
            }
        }
        Collections.sort(root.getChildren(), projectModelComparator);
    }

    /**
     * A comparator for ProjectModel instances that does two things.
     * a) sorts the TemplateProjectModels to the end
     * b) within common project models (Template/Concrete) it sorts using a delegate
     *    comparator.
     */
    private static class ProjectModelComparator implements Comparator<ProjectModel>
    {
        private Comparator<String> delegate;

        private ProjectModelComparator(Comparator<String> delegate)
        {
            this.delegate = delegate;
        }

        public int compare(ProjectModel o1, ProjectModel o2)
        {
            if (o1 instanceof TemplateProjectModel && o2 instanceof TemplateProjectModel)
            {
                return delegate.compare(o1.getName(), o2.getName());
            }
            if (o1 instanceof TemplateProjectModel)
            {
                return 1;
            }
            if (o2 instanceof TemplateProjectModel)
            {
                return -1;
            }

            return delegate.compare(o1.getName(), o2.getName());
        }
    }

}
