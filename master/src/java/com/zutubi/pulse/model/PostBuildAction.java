package com.zutubi.pulse.model;

import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.util.logging.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * Post build actions are run after a build, and may be restricted to certain
 * result states.
 */
public abstract class PostBuildAction extends Entity
{
    private static final Logger LOG = Logger.getLogger(PostBuildAction.class);

    private String name;
    private List<ResultState> states = new LinkedList<ResultState>();
    private boolean failOnError = false;
    private List<String> errors;

    protected PostBuildAction()
    {
    }

    protected PostBuildAction(String name, List<ResultState> states, boolean failOnError)
    {
        this.name = name;
        this.states = states;
        this.failOnError = failOnError;
    }

    protected void copyCommon(PostBuildAction copy)
    {
        copy.name = name;
        copy.states = new LinkedList<ResultState>(states);
        copy.failOnError = failOnError;
    }

    public void execute(ProjectConfiguration projectConfig, BuildResult build, RecipeResultNode recipe, List<ResourceProperty> properties)
    {
        errors = new LinkedList<String>();

        if(resultMatches(build))
        {
            internalExecute(projectConfig, build, recipe, properties);
            for(String error: errors)
            {
                if(failOnError)
                {
                    if(recipe == null)
                    {
                        build.error(error);
                    }
                    else
                    {
                        recipe.getResult().error(error);
                    }
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
        return stateMatches(result);
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

    protected abstract void internalExecute(ProjectConfiguration projectConfig, BuildResult build, RecipeResultNode recipe, List<ResourceProperty> properties);

    public abstract String getType();

    public abstract PostBuildAction copy();
}
