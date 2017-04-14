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

package com.zutubi.pulse.dev.expand;

import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.engine.ResourcesConfiguration;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.engine.api.Scope;
import com.zutubi.pulse.core.engine.marshal.PulseFileLoader;
import com.zutubi.pulse.core.engine.marshal.PulseFileLoaderFactory;
import com.zutubi.pulse.core.engine.marshal.ResourceFileLoader;
import com.zutubi.pulse.core.marshal.*;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.IOUtils;
import nu.xom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Utility class that loads and stores a Pulse file.  This is useful for
 * debugging, as during loading properties, imports, macros etc are processed,
 * so the resulting output allows the user to see how the file "expands".
 */
public class PulseFileExpander
{
    private PulseFileLoaderFactory fileLoaderFactory;
    private ResourceFileLoader resourceFileLoader;

    /**
     * Expands a pulse file specified by the given options.
     *  
     * @param options options controlling the expansion process
     * @throws IOException    if there is an error reading/writing a file
     * @throws PulseException on any other error
     */
    public void expand(PulseFileExpanderOptions options) throws IOException, PulseException
    {
        ProjectRecipesConfiguration recipes = loadRecipes(options);
        ToveFileStorer storer = fileLoaderFactory.createStorer();
        storer.store(options.getOutputStream(), recipes, new Element("project"));
    }

    private ProjectRecipesConfiguration loadRecipes(PulseFileExpanderOptions options) throws PulseException, IOException
    {
        PulseFileLoader pulseFileLoader = fileLoaderFactory.createLoader();
        ProjectRecipesConfiguration recipes = new ProjectRecipesConfiguration();
        File pulseFile = new File(options.getPulseFile());
        if (!pulseFile.isAbsolute())
        {
            pulseFile = new File(options.getBaseDir(), options.getPulseFile());
        }

        String recipe = options.getRecipe();

        InputStream is = null;
        try
        {
            is = new FileInputStream(pulseFile);
            pulseFileLoader.load(is, recipes, createScope(options), new RelativeFileResolver(options.getPulseFile(), new LocalFileResolver(options.getBaseDir())), getLoadPredicate(recipes, recipe));
        }
        finally
        {
            IOUtils.close(is);
        }

        if (StringUtils.stringSet(recipe))
        {
            recipes = isolateRecipe(recipes, recipe);
        }
        
        return recipes;
    }

    private Scope createScope(PulseFileExpanderOptions options) throws IOException, PulseException
    {
        PulseExecutionContext context = new PulseExecutionContext();
        InMemoryResourceRepository resourceRepository = createResourceRepository(options);
        RecipeUtils.addResourceProperties(context, options.getResourceRequirements(), resourceRepository);

        PulseScope scope = context.getScope();
        for (Map.Entry define: options.getDefines().entrySet())
        {
            scope.add(new ResourceProperty((String) define.getKey(), (String) define.getValue()));
        }
        
        return scope;
    }

    private InMemoryResourceRepository createResourceRepository(PulseFileExpanderOptions options) throws IOException, PulseException
    {
        InMemoryResourceRepository resourceRepository = new InMemoryResourceRepository();
        String resourcesFile = options.getResourcesFile();
        if (StringUtils.stringSet(resourcesFile))
        {
            ResourcesConfiguration resourcesConfiguration = resourceFileLoader.load(new File(resourcesFile));
            resourceRepository.addAllResources(resourcesConfiguration.getResources().values());
        }
        
        return resourceRepository;
    }

    private ToveFileLoadInterceptor getLoadPredicate(ProjectRecipesConfiguration recipes, String recipe)
    {
        if (StringUtils.stringSet(recipe))
        {
            return new RecipeLoadInterceptor(recipes, recipe);
        }
        else
        {
            return new DefaultToveFileLoadInterceptor();
        }
    }

    private ProjectRecipesConfiguration isolateRecipe(ProjectRecipesConfiguration recipes, String recipe) throws PulseException
    {
        RecipeConfiguration recipeConfig = recipes.getRecipes().get(recipe);
        if (recipeConfig == null)
        {
            List<String> allRecipes = new LinkedList<String>(recipes.getRecipes().keySet());
            Collections.sort(allRecipes);
            throw new PulseException("Recipe '" + recipe + "' does not exist.  Available recipes are: " + allRecipes + ".");
        }
        
        ProjectRecipesConfiguration isolated = new ProjectRecipesConfiguration();
        isolated.addRecipe(recipeConfig);
        return isolated;
    }

    public void setFileLoaderFactory(PulseFileLoaderFactory fileLoaderFactory)
    {
        this.fileLoaderFactory = fileLoaderFactory;
    }

    public void setResourceFileLoader(ResourceFileLoader resourceFileLoader)
    {
        this.resourceFileLoader = resourceFileLoader;
    }
}
