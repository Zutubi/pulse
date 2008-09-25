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

        RecipeErrorEvent event = (RecipeErrorEvent) o;
        return errorMessage.equals(event.errorMessage);
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + errorMessage.hashCode();
        return result;
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Recipe Error Event");
        buff.append(": ").append(getRecipeId()).append(": ").append(errorMessage);
        return buff.toString();
    }    
}
