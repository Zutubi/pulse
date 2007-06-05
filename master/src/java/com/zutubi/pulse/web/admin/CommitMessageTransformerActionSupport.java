package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.committransformers.CommitMessageTransformerManager;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.web.ActionSupport;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 */
public class CommitMessageTransformerActionSupport extends ActionSupport
{
    private long id;

    private Map<Long, String> allProjects;
    private List<Long> selectedProjects = new LinkedList<Long>();

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Map<Long, String> getAllProjects()
    {
        if(allProjects == null)
        {
            // FIXME: sort the map.
            allProjects = new LinkedHashMap<Long, String>();
            Collection<ProjectConfiguration> all = projectManager.getAllProjectConfigs();
            for(ProjectConfiguration p: all)
            {
                allProjects.put(p.getHandle(), p.getName());
            }
        }
        return allProjects;
    }

    public List<Long> getSelectedProjects()
    {
        return selectedProjects;
    }

    public void setSelectedProjects(List<Long> selectedProjects)
    {
        this.selectedProjects = selectedProjects;
    }

    public CommitMessageTransformerManager getTransformerManager()
    {
        return commitMessageTransformerManager;
    }
}
