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

package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.RecipeProcessor;
import com.zutubi.pulse.core.RecipeRequest;

/**
 * Interface for instances that can run recipes on a processor.  Allows
 * implementations to wrap setup/teardown activities around the recipe
 * processing, e.g. additional context setup.
 */
public interface RecipeRunner
{
    /**
     * Runs the given recipe using the given processor.
     *
     * @param request         request providing the recipe to run
     * @param recipeProcessor the processor to use for running the recipe
     */
    public void runRecipe(RecipeRequest request, RecipeProcessor recipeProcessor);
}
