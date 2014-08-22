package com.zutubi.pulse.core.events;

import com.zutubi.events.Event;

/**
 */
public class RecipeEvent extends Event
{
    private final long buildId;
    private final long recipeId;

    public RecipeEvent(Object source, long buildId, long recipeId)
    {
        super(source);
        this.buildId = buildId;
        this.recipeId = recipeId;
    }

    /**
     * @return id of the build result we belong to, or 0 if the recipe is not part of a larger build
     */
    public long getBuildId()
    {
        return buildId;
    }

    public long getRecipeId()
    {
        return recipeId;
    }

    @Override
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

        RecipeEvent that = (RecipeEvent) o;

        if (buildId != that.buildId)
        {
            return false;
        }
        if (recipeId != that.recipeId)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = (int) (buildId ^ (buildId >>> 32));
        result = 31 * result + (int) (recipeId ^ (recipeId >>> 32));
        return result;
    }

    public String toString()
    {
        return "Recipe Event" + ": " + getRecipeId();
    }
}
