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

    public void add(CommandResult result)
    {
        results.add(result);
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

    public void commandError()
    {
        state = ResultState.ERROR;
    }

}
