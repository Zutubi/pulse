package com.cinnamonbob.core.model;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class RecipeResult extends Result
{
    private String recipeName;
    private List<CommandResult> results;

    public RecipeResult()
    {
    }

    public RecipeResult(String recipeName)
    {
        this.recipeName = recipeName;
        state = ResultState.INITIAL;
        results = new LinkedList<CommandResult>();
    }

    public void commence(String recipeName, long startTime)
    {
        this.recipeName = recipeName;
        super.commence(startTime);
    }

    public void add(CommandResult result)
    {
        results.add(result);
    }

    public void update(CommandResult result)
    {
        results.remove(results.size() - 1);
        results.add(result);

        switch (result.state)
        {
            case ERROR:
            case FAILURE:
                state = ResultState.ERROR;
                break;
        }
    }

    public void update(RecipeResult result)
    {
        this.state = result.state;
        this.stamps = result.stamps;
        this.errorMessage = result.errorMessage;
    }

    public List<CommandResult> getCommandResults()
    {
        return results;
    }

    private void setCommandResults(List<CommandResult> results)
    {
        this.results = results;
    }

    public String getRecipeName()
    {
        return recipeName;
    }

    private void setRecipeName(String recipeName)
    {
        this.recipeName = recipeName;
    }
}
