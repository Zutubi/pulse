package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.scheduling.BuildCompletedEventFilter;
import com.zutubi.pulse.scheduling.EventTrigger;
import com.zutubi.pulse.scheduling.Trigger;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 */
public class EditBuildCompletedTriggerAction extends AbstractEditTriggerAction
{
    private EventTrigger trigger = new EventTrigger();
    private BuildCompletedTriggerHelper helper = new BuildCompletedTriggerHelper();
    private Long filterProject;
    private String filterSpecification;
    private List<String> filterStateNames;

    public Long getFilterProject()
    {
        return filterProject;
    }

    public void setFilterProject(Long filterProject)
    {
        this.filterProject = filterProject;
    }

    public String getFilterSpecification()
    {
        return filterSpecification;
    }

    public void setFilterSpecification(String filterSpecification)
    {
        this.filterSpecification = filterSpecification;
    }

    public List<String> getFilterStateNames()
    {
        return filterStateNames;
    }

    public void setFilterStateNames(List<String> filterStateNames)
    {
        this.filterStateNames = filterStateNames;
    }

    public void prepare() throws Exception
    {
        helper.initialise(getProjectManager());

        Trigger t = getScheduler().getTrigger(getId());
        if (t == null)
        {
            addActionError("Unknown trigger [" + getId() + "]");
            return;
        }

        if (!(t instanceof EventTrigger))
        {
            addActionError("Invalid trigger type '" + t.getType() + "'");
            return;
        }

        trigger = (EventTrigger) t;

        Map<Serializable, Serializable> dataMap = trigger.getDataMap();
        filterProject = (Long) dataMap.get(BuildCompletedEventFilter.PARAM_PROJECT);
        filterSpecification = (String) dataMap.get(BuildCompletedEventFilter.PARAM_SPECIFICATION);
        String states = (String) dataMap.get(BuildCompletedEventFilter.PARAM_STATES);
        filterStateNames = new LinkedList<String>();
        for(ResultState r: BuildCompletedEventFilter.getStates(states))
        {
            filterStateNames.add(r.toString());
        }
        
        // Must set trigger before calling super
        super.prepare();
    }

    public Map<Long, String> getFilterProjects()
    {
        return helper.getFilterProjects();
    }

    public Map<Long, List<String>> getFilterSpecifications()
    {
        return helper.getFilterSpecifications();
    }

    public Map<String, String> getStateMap()
    {
        return helper.getStateMap();
    }

    public EventTrigger getTrigger()
    {
        return trigger;
    }

    public String execute()
    {
        helper.populateTrigger(trigger, filterProject, filterSpecification, filterStateNames);
        return super.execute();
    }
}
