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
 * A recipe event that just carries a status message for the recipe log.
 */
public class RecipeStatusEvent extends RecipeEvent
{
    private String message;

    public RecipeStatusEvent(Object source, long buildId, long recipeId, String message)
    {
        super(source, buildId, recipeId);
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
