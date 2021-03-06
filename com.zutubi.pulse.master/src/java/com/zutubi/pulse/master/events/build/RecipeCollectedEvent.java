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

/**
 * This event is raised by the build controller when a recipe is completed
 * and the artifacts have been collected/cleaned up on the slave.  Note this
 * is also after the post stage actions have run.
 */
public class RecipeCollectedEvent extends RecipeEvent
{
    public RecipeCollectedEvent(Object source, long buildId, long recipeId)
    {
        super(source, buildId, recipeId);
    }

    public String toString()
    {
        return "Recipe Collected Event" + ": " + getRecipeId();
    }
}
