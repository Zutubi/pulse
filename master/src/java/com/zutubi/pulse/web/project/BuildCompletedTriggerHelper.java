package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.scheduling.BuildCompletedEventFilter;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;

/**
 */
public class BuildCompletedTriggerHelper
{
    private Map<Long, String> filterProjects;

    public void initialise(ProjectManager projectManager)
    {
        // FIXME: sort the map.
        filterProjects = new TreeMap<Long, String>();
        Collection<ProjectConfiguration> all = projectManager.getAllProjectConfigs();
        for(ProjectConfiguration p: all)
        {
            filterProjects.put(p.getHandle(), p.getName());
        }
    }

    public void populateTrigger(Trigger trigger, Long filterProject, String filterSpecification, List<String> filterStateNames)
    {
        Map<Serializable, Serializable> dataMap = trigger.getDataMap();
        dataMap.put(BuildCompletedEventFilter.PARAM_PROJECT, filterProject);

        if(filterStateNames != null && filterStateNames.size() > 0)
        {
            dataMap.put(BuildCompletedEventFilter.PARAM_STATES, ResultState.getStateNamesString(filterStateNames));
        }
    }

    public Map<Long, String> getFilterProjects()
    {
        return filterProjects;
    }

    public Map<String, String> getStateMap()
    {
        Map<String, String> result = new TreeMap<String, String>();

        result.put(ResultState.ERROR.toString(), "error");
        result.put(ResultState.FAILURE.toString(), "failure");
        result.put(ResultState.SUCCESS.toString(), "success");

        return result;
    }
}
