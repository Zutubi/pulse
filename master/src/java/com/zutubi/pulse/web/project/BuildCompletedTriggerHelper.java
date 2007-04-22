package com.zutubi.pulse.web.project;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.NamedEntityComparator;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.scheduling.BuildCompletedEventFilter;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.util.Sort;

import java.io.Serializable;
import java.util.*;

/**
 */
public class BuildCompletedTriggerHelper
{
    private Map<Long, String> filterProjects;
    private Map<Long, List<String>> filterSpecifications;

    public void initialise(ProjectManager projectManager)
    {
        filterProjects = new TreeMap<Long, String>();
        filterSpecifications = new LinkedHashMap<Long, List<String>>();

        List<Project> projects = projectManager.getNameToConfig();
        Collections.sort(projects, new NamedEntityComparator());
        for(Project p: projects)
        {
            filterProjects.put(p.getId(), p.getName());

            List<String> specs = new LinkedList<String>();
            for(BuildSpecification spec: p.getBuildSpecifications())
            {
                specs.add(spec.getName());
            }

            Collections.sort(specs, new Sort.StringComparator());
            specs.add(0, "");
            filterSpecifications.put(p.getId(), specs);
        }
    }

    public void populateTrigger(Trigger trigger, Long filterProject, String filterSpecification, List<String> filterStateNames)
    {
        Map<Serializable, Serializable> dataMap = trigger.getDataMap();
        dataMap.put(BuildCompletedEventFilter.PARAM_PROJECT, filterProject);

        if(TextUtils.stringSet(filterSpecification))
        {
            dataMap.put(BuildCompletedEventFilter.PARAM_SPECIFICATION, filterSpecification);
        }

        if(filterStateNames != null && filterStateNames.size() > 0)
        {
            dataMap.put(BuildCompletedEventFilter.PARAM_STATES, ResultState.getStateNamesString(filterStateNames));
        }
    }

    public Map<Long, String> getFilterProjects()
    {
        return filterProjects;
    }

    public Map<Long, List<String>> getFilterSpecifications()
    {
        return filterSpecifications;
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
