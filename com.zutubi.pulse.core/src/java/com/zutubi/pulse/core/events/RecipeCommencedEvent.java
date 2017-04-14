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

import java.util.Map;

/**
 * This event is raised by the recipe processor when commencing a recipe.
 */
public class RecipeCommencedEvent extends RecipeEvent
{
    private String name;
    private Map<String, String> pathProperties;
    private long startTime;

    public RecipeCommencedEvent(Object source, long buildId, long recipeId, String name, Map<String, String> pathProperties, long startTime)
    {
        super(source, buildId, recipeId);
        this.name = name;
        this.pathProperties = pathProperties;
        this.startTime = startTime;
    }

    public String getName()
    {
        return name;
    }

    public Map<String, String> getPathProperties()
    {
        return pathProperties;
    }

    public long getStartTime()
    {
        return startTime;
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
        if (!super.equals(o))
        {
            return false;
        }

        RecipeCommencedEvent that = (RecipeCommencedEvent) o;

        if (startTime != that.startTime)
        {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }
        return !(pathProperties != null ? !pathProperties.equals(that.pathProperties) : that.pathProperties != null);

    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (pathProperties != null ? pathProperties.hashCode() : 0);
        result = 31 * result + (int) (startTime ^ (startTime >>> 32));
        return result;
    }

    public String toString()
    {
        return "Recipe Commenced Event" + ": " + getRecipeId();
    }    
}
