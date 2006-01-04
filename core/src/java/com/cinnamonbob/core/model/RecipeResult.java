package com.cinnamonbob.core.model;

import java.io.File;
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
        // lets save this command result by replacing the existing persistent result
        // with the new one... simple.
        CommandResult currentResult = results.remove(results.size() - 1);
        result.setId(currentResult.getId());
        results.add(result);
        File remoteDir = new File(result.getOutputDir());
        File localDir = new File(getOutputDir(), remoteDir.getName());
        result.setOutputDir(localDir.getAbsolutePath());

        switch (result.state)
        {
            case ERROR:
            case FAILURE:
                state = ResultState.FAILURE;
                break;
        }
    }

    public void update(RecipeResult result)
    {
        // Don't unconditionally take state: take it only if it may indicate
        // an error which we may not have noticed locally.
        switch (result.state)
        {
            case ERROR:
            case FAILURE:
                state = result.state;
        }

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
