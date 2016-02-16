package com.zutubi.pulse.master.tove.config.project;

import com.google.common.base.Function;
import com.zutubi.pulse.master.model.ProjectGroup;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.handler.FormContext;
import com.zutubi.tove.ui.handler.ListOptionProvider;
import com.zutubi.util.Sort;
import com.zutubi.util.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.collect.Collections2.transform;

/**
 */
public class ProjectLabelOptionProvider extends ListOptionProvider
{
    private static final Logger LOG = Logger.getLogger(ProjectLabelOptionProvider.class);

    private ProjectManager projectManager;

    public String getEmptyOption(TypeProperty property, FormContext context)
    {
        return null;
    }

    public List<String> getOptions(TypeProperty property, FormContext context)
    {
        Set<String> sortedLabels = new TreeSet<>(new Sort.StringComparator());
        if (context.getExistingInstance() != null)
        {
            try
            {
                Object propertyValue = property.getValue(context.getExistingInstance());
                if(propertyValue instanceof List)
                {
                    sortedLabels.addAll((List<String>)propertyValue);
                }
            }
            catch (Exception e)
            {
                LOG.severe(e);
            }
        }

        sortedLabels.addAll(transform(projectManager.getAllProjectGroups(), new Function<ProjectGroup, String>()
        {
            public String apply(ProjectGroup projectGroup)
            {
                return projectGroup.getName();
            }
        }));

        return new ArrayList<>(sortedLabels);
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
