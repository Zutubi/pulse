/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    private TemplateSortStrategy templateSortStrategy = TemplateSortStrategy.MIXED;

    public void setGroupNameComparator(Comparator<String> groupNameComparator)
    {
        this.groupNameComparator = groupNameComparator;
    }

    public void setProjectNameComparator(Comparator<String> projectNameComparator)
    {
        this.projectNameComparator = projectNameComparator;
    }

    public void sortTemplatesToStart()
    {
        templateSortStrategy = TemplateSortStrategy.TO_START;
    }

    public void sortTemplatesToEnd()
    {
        templateSortStrategy = TemplateSortStrategy.TO_END;
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
        
        final Comparator<ProjectModel> projectModelComparator = new ProjectModelComparator(projectNameComparator, templateSortStrategy);
        
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

        private TemplateSortStrategy strategy;

        private ProjectModelComparator(Comparator<String> delegate, TemplateSortStrategy strategy)
        {
            this.delegate = delegate;
            this.strategy = strategy;
        }

        public int compare(ProjectModel o1, ProjectModel o2)
        {
            if (o1 instanceof TemplateProjectModel && o2 instanceof TemplateProjectModel)
            {
                return delegate.compare(o1.getName(), o2.getName());
            }

            switch (strategy)
            {
                case TO_START:
                    if (o1 instanceof TemplateProjectModel)
                    {
                        return -1;
                    }
                    if (o2 instanceof TemplateProjectModel)
                    {
                        return 1;
                    }
                case TO_END:
                    if (o1 instanceof TemplateProjectModel)
                    {
                        return 1;
                    }
                    if (o2 instanceof TemplateProjectModel)
                    {
                        return -1;
                    }
                case MIXED:
                    break;
            }

            return delegate.compare(o1.getName(), o2.getName());
        }
    }

    public enum TemplateSortStrategy
    {
        TO_START, TO_END, MIXED
    }
}
