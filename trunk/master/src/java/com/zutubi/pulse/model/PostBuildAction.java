package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.util.logging.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * Post build actions are run after a build, but may be restricted to certain
 * build specifications and result states.
 */
public abstract class PostBuildAction extends Entity
{
    private static final Logger LOG = Logger.getLogger(PostBuildAction.class);

    private String name;
    private List<BuildSpecification> specifications = new LinkedList<BuildSpecification>();
    private List<ResultState> states = new LinkedList<ResultState>();
    private boolean failOnError = false;
    private List<String> errors;

    protected PostBuildAction()
    {
    }

    protected PostBuildAction(String name, List<BuildSpecification> specifications, List<ResultState> states, boolean failOnError)
    {
        this.name = name;
        this.specifications = specifications;
        this.states = states;
        this.failOnError = failOnError;
    }

    protected void copyCommon(PostBuildAction copy)
    {
        copy.name = name;
        copy.specifications = new LinkedList<BuildSpecification>(specifications);
        copy.states = new LinkedList<ResultState>(states);
        copy.failOnError = failOnError;
    }

    public void execute(BuildResult result)
    {
        errors = new LinkedList<String>();

        if(resultMatches(result))
        {
            internalExecute(result);
            for(String error: errors)
            {
                if(failOnError)
                {
                    result.error(error);
                }
                else
                {
                    LOG.warning(error);
                }
            }
        }
    }

    private boolean resultMatches(BuildResult result)
    {
        return specMatches(result) && stateMatches(result);
    }

    private boolean specMatches(BuildResult result)
    {
        if(specifications == null || specifications.size() == 0)
        {
            return true;
        }

        for(BuildSpecification spec: specifications)
        {
            if(spec.getName().equals(result.getBuildSpecification()))
            {
                return true;
            }
        }

        return false;
    }

    private boolean stateMatches(BuildResult result)
    {
        if(states == null || states.size() == 0)
        {
            return true;
        }

        for(ResultState state: states)
        {
            if(state.equals(result.getState()))
            {
                return true;
            }
        }

        return false;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<BuildSpecification> getSpecifications()
    {
        return specifications;
    }

    public void setSpecifications(List<BuildSpecification> specifications)
    {
        this.specifications = specifications;
    }

    public List<Long> getBuildSpecificationIds()
    {
        List<Long> ids = new LinkedList<Long>();
        for(BuildSpecification spec: specifications)
        {
            ids.add(spec.getId());
        }

        return ids;
    }

    public List<ResultState> getStates()
    {
        return states;
    }

    public void setStates(List<ResultState> states)
    {
        this.states = states;
    }

    public String getStatesString()
    {
        return ResultState.getStatesString(states);
    }

    public void setStatesString(String value)
    {
        states = ResultState.getStatesList(value);
    }

    public boolean getFailOnError()
    {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError)
    {
        this.failOnError = failOnError;
    }

    protected void addError(String error)
    {
        errors.add("Executing post build action '" + name + "': " + error);
    }

    protected abstract void internalExecute(BuildResult result);

    public abstract String getType();

    public abstract PostBuildAction copy();
}
