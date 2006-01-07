package com.cinnamonbob.events.build;

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
}
