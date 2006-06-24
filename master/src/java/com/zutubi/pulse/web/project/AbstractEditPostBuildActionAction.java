package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.NamedEntityComparator;
import com.zutubi.pulse.model.PostBuildAction;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.xwork.interceptor.Preparable;

import java.util.*;

/**
 *
 */
public abstract class AbstractEditPostBuildActionAction extends ProjectActionSupport implements Preparable
{
    private long id;
    private Project project;

    private String newName;
    private boolean failOnError = false;
    private List<Long> specIds;
    private List<String> stateNames;

    private Map<Long, String> specs;
    private Map<String, String> states;

    private static final List<String> ID_PARAMS = Arrays.asList("id", "projectId");

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
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

    public Map<Long, String> getSpecs()
    {
        if(specs == null)
        {
            specs = new LinkedHashMap<Long, String>();
            List<BuildSpecification> buildSpecifications = project.getBuildSpecifications();
            Collections.sort(buildSpecifications, new NamedEntityComparator());
            for (BuildSpecification spec : buildSpecifications)
            {
                specs.put(spec.getId(), spec.getName());
            }
        }

        return specs;
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
            return;
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
        specIds = getPostBuildAction().getBuildSpecificationIds();
        stateNames = ResultState.getNames(getPostBuildAction().getStates());
        return INPUT;
    }

    public void validate()
    {
        if(hasErrors())
        {
            return;
        }

        PostBuildAction a = getProject().getPostBuildAction(newName);
        if(a != null && a.getId() != getPostBuildAction().getId())
        {
            addFieldError("newName", "This project already has a post build action with name '" + newName + "'");
        }
    }

    public String execute()
    {
        if (hasErrors())
        {
            return ERROR;
        }

        PostBuildAction action = getPostBuildAction();
        action.setName(newName);
        action.setSpecifications(project.lookupBuildSpecifications(specIds));
        action.setStates(ResultState.getStatesList(stateNames));
        action.setFailOnError(failOnError);
        getProjectManager().save(project);
        return SUCCESS;
    }

    public abstract PostBuildAction getPostBuildAction();
}
