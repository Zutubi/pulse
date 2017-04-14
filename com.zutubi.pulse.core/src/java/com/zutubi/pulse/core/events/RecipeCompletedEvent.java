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

import com.zutubi.pulse.core.model.RecipeResult;

/**
 * This event is raised by the recipe processor when a recipe is completed.
 */
public class RecipeCompletedEvent extends RecipeEvent
{
    private RecipeResult result;

    private RecipeCompletedEvent()
    {
        // For hessian
        super(null, 0, 0);
    }

    public RecipeCompletedEvent(Object source, long buildId, RecipeResult result)
    {
        super(source, buildId, result.getId());
        this.result = result;
    }

    public RecipeResult getResult()
    {
        return result;
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

        RecipeCompletedEvent event = (RecipeCompletedEvent) o;
        return result.equals(event.result);
    }

    public int hashCode()
    {
        int result1 = super.hashCode();
        result1 = 31 * result1 + result.hashCode();
        return result1;
    }

    public String toString()
    {
        return "Recipe Completed Event" + ": " + getRecipeId();
    }    
}
