package com.zutubi.pulse.core.events;

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

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        RecipeStatusEvent event = (RecipeStatusEvent) o;
        return message.equals(event.message);
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + message.hashCode();
        return result;
    }

    public String toString()
    {
        return "Recipe Status Event" + ": " + getRecipeId() + ": " + message;
    }    
}
