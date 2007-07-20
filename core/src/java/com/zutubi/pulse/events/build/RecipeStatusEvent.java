package com.zutubi.pulse.events.build;

/**
 * A recipe event that just carries a status message for the recipe log.
 */
public class RecipeStatusEvent extends RecipeEvent
{
    private String message;

    public RecipeStatusEvent(Object source, long recipeId, String message)
    {
        super(source, recipeId);
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }

    public String toString()
    {
        return new StringBuilder("Recipe Status Event").append(": ").append(getRecipeId()).append(": ").append(message).toString();
    }    
}
