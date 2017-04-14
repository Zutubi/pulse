/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
