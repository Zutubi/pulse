package com.zutubi.pulse.core.events;

/**
 */
public class RecipeErrorEvent extends RecipeEvent
{
    private String errorMessage;

    public RecipeErrorEvent(Object source, long recipeId, String errorMessage)
    {
        super(source, recipeId);
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Recipe Error Event");
        buff.append(": ").append(getRecipeId()).append(": ").append(errorMessage);
        return buff.toString();
    }    
}
