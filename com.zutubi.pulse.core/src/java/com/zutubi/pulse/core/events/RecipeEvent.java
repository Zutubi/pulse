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
