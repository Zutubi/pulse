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

package com.zutubi.pulse.core.engine.marshal;

import com.zutubi.pulse.core.PulseScope;
import com.zutubi.pulse.core.RecipeListingInterceptor;
import com.zutubi.pulse.core.RecipeLoadInterceptor;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.marshal.FileResolver;
import com.zutubi.pulse.core.marshal.ToveFileLoader;
import com.zutubi.tove.config.api.Configurations;

import java.io.ByteArrayInputStream;
import java.util.List;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Convenience class for common pulse file loading operations.
 */
public class PulseFileLoader extends ToveFileLoader
{
    /**
     * Loads a pulse file, isolating a single recipe.
     *
     * @param pulseFile    source of the pulse file (an XML string)
     * @param recipes      root recipes object to load into
     * @param interceptor  interceptor to load with
     * @param globalScope  scope to use as basis for loading
     * @param fileResolver used to resolve any imported files
     * @return configuration for the loaded recipe
     * @throws PulseException if the file cannot be loaded
     */
    public void loadRecipe(String pulseFile, ProjectRecipesConfiguration recipes, RecipeLoadInterceptor interceptor, PulseScope globalScope, FileResolver fileResolver) throws PulseException
    {
        load(new ByteArrayInputStream(pulseFile.getBytes()), recipes, globalScope, fileResolver, interceptor);
    }

    /**
     * Retrieves a list of available recipes from a pulse file.
     *
     * @param pulseFile    source of the pulse file (an XML string)
     * @param fileResolver used to resolve any imported files
     * @return a list of all recipe names in the file
     * @throws PulseException if the file cannot be loaded
     */
    public List<String> loadAvailableRecipes(String pulseFile, FileResolver fileResolver) throws PulseException
    {
        ProjectRecipesConfiguration recipes = new ProjectRecipesConfiguration();
        RecipeListingInterceptor predicate = new RecipeListingInterceptor();
        load(new ByteArrayInputStream(pulseFile.getBytes()), recipes, new PulseScope(), fileResolver, predicate);

        return newArrayList(transform(recipes.getRecipes().values(), Configurations.toConfigurationName()));
    }
}
