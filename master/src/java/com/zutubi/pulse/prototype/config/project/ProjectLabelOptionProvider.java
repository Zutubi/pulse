package com.zutubi.pulse.prototype.config.project;

import com.zutubi.prototype.ListOptionProvider;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.pulse.model.ProjectGroup;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Sort;
import com.zutubi.util.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 */
public class ProjectLabelOptionProvider extends ListOptionProvider
{
    private static final Logger LOG = Logger.getLogger(ProjectLabelOptionProvider.class);

    private ProjectManager projectManager;

    public String getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        return null;
    }

    public List<String> getOptions(Object instance, String parentPath, TypeProperty property)
    {
        Set<String> sortedLabels = new TreeSet<String>(new Sort.StringComparator());
        if(instance != null)
        {
            try
            {
                Object propertyValue = property.getValue(instance);
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

        CollectionUtils.map(projectManager.getAllProjectGroups(), new Mapping<ProjectGroup, String>()
        {
            public String map(ProjectGroup projectGroup)
            {
                return projectGroup.getName();
            }
        }, sortedLabels);

        return new ArrayList<String>(sortedLabels);
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
