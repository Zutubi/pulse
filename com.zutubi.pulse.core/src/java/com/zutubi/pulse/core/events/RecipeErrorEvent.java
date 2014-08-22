package com.zutubi.pulse.core.events;

/**
 */
public class RecipeErrorEvent extends RecipeEvent
{
    private String errorMessage;
    private boolean agentStatusProblem;

    public RecipeErrorEvent(Object source, long buildId, long recipeId, String errorMessage, boolean agentStatusProblem)
    {
        super(source, buildId, recipeId);
        this.errorMessage = errorMessage;
        this.agentStatusProblem = agentStatusProblem;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    /**
     * @return true if this error was caused by an agent timeout - either the agent being unexpectedly idle, or the
     *         connection being lost altogether during the recipe
     */
    public boolean isAgentStatusProblem()
    {
        return agentStatusProblem;
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
        return "Recipe Error Event" + ": " + getRecipeId() + ": " + errorMessage;
    }    
}
