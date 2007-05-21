package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.PostBuildAction;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.xwork.interceptor.Preparable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 */
public abstract class AbstractEditPostBuildActionAction extends ProjectActionSupport implements Preparable
{
    private long id;
    private long specId = 0;
    private long nodeId = 0;
    private Project project;

    private String newName;
    private boolean failOnError = false;
    private List<Long> specIds;
    private List<String> stateNames;

    private Map<String, String> states;

    private static final List<String> ID_PARAMS = Arrays.asList("id", "projectId", "nodeId");

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getSpecId()
    {
        return specId;
    }

    public void setSpecId(long specId)
    {
        this.specId = specId;
    }

    public long getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(long nodeId)
    {
        this.nodeId = nodeId;
    }

    public Project getProject()
    {
        return project;
    }

    public String getNewName()
    {
        return newName;
    }

    public void setNewName(String newName)
    {
        this.newName = newName;
    }

    public boolean getFailOnError()
    {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError)
    {
        this.failOnError = failOnError;
    }

    public List<Long> getSpecIds()
    {
        return specIds;
    }

    public void setSpecIds(List<Long> specIds)
    {
        this.specIds = specIds;
    }

    public List<String> getStateNames()
    {
        return stateNames;
    }

    public void setStateNames(List<String> stateNames)
    {
        this.stateNames = stateNames;
    }

    public boolean isStage()
    {
        return specId > 0;
    }

    public Map<String, String> getStates()
    {
        if(states == null)
        {
            states = ResultState.getCompletedStatesMap();
        }

        return states;
    }

    public List<String> getPrepareParameterNames()
    {
        return AbstractEditPostBuildActionAction.ID_PARAMS;
    }

    public void prepare() throws Exception
    {
        project = getProjectManager().getProject(projectId);
        if (project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
        }
    }

    public String doInput()
    {
        if (hasErrors())
        {
            return ERROR;
        }

        newName = getPostBuildAction().getName();
        failOnError = getPostBuildAction().getFailOnError();
        stateNames = ResultState.getNames(getPostBuildAction().getStates());
        return INPUT;
    }

    public void validate()
    {
        if(hasErrors())
        {
            return;
        }

/*
        PostBuildAction a = getProject().getPostBuildAction(newName);
        if(a != null && a.getId() != getPostBuildAction().getId())
        {
            addFieldError("newName", "This project already has a post build action with name '" + newName + "'");
        }
*/
    }

    public String execute()
    {
        if (hasErrors())
        {
            return ERROR;
        }

        PostBuildAction action = getPostBuildAction();
        action.setName(newName);
        action.setStates(ResultState.getStatesList(stateNames));
        action.setFailOnError(failOnError);
        getProjectManager().save(project);
        return SUCCESS;
    }

    protected PostBuildAction lookupAction()
    {
/*
        PostBuildAction a = project.getPostBuildAction(getId());
        if (a == null)
        {
            addActionError("Unknown post build action [" + getId() + "]");
        }

        return a;
*/
        return null;
    }

    public abstract PostBuildAction getPostBuildAction();
}
