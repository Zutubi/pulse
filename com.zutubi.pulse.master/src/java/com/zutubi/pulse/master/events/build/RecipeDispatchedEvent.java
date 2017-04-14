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

package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.events.RecipeEvent;
import com.zutubi.pulse.master.agent.Agent;

/**
 * Raised when a recipe has been dispatched to an agent.
 */
public class RecipeDispatchedEvent extends RecipeEvent
{
    /**
     * The agent the recipe was dispatched to.
     */
    private Agent agent;

    /**
     * @param source   source of the event
     * @param buildId id of the build the recipe is part of
     * @param recipeId id of the recipe that has been dispatched
     * @param agent    agent the recipe was dispatched to
     */
    public RecipeDispatchedEvent(Object source, long buildId, long recipeId, Agent agent)
    {
        super(source, buildId, recipeId);
        this.agent = agent;
    }

    public Agent getAgent()
    {
        return agent;
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

        RecipeDispatchedEvent event = (RecipeDispatchedEvent) o;
        return agent.equals(event.agent);
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + agent.hashCode();
        return result;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder("Recipe Dispatched Event");
        builder.append(": ").append(getRecipeId());
        if (getAgent() != null)
        {
            builder.append(": ").append(getAgent().getConfig().getName());
        }
        return builder.toString();
    }
}
