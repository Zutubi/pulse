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

package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.engine.FixedPulseFileProvider;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.PulseFileProvider;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.engine.marshal.PulseFileLoaderFactory;
import com.zutubi.pulse.core.marshal.ToveFileStorer;
import com.zutubi.tove.annotations.*;
import com.zutubi.util.io.IOUtils;
import nu.xom.Element;

import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A project type that allows multiple recipes to be fully configured in the
 * UI.
 */
@SymbolicName("zutubi.multiRecipeTypeConfig")
@Wire
@Form(fieldOrder = {"defaultRecipe"})
public class MultiRecipeTypeConfiguration extends TypeConfiguration
{
    @Combobox(optionProvider = "MultiRecipeTypeDefaultRecipeOptionProvider")
    @Wizard.Ignore
    private String defaultRecipe;
    private Map<String, RecipeConfiguration> recipes = new LinkedHashMap<String, RecipeConfiguration>();
    @Transient
    private PulseFileLoaderFactory fileLoaderFactory;

    public PulseFileProvider getPulseFile() throws Exception
    {
        ProjectRecipesConfiguration prc = new ProjectRecipesConfiguration();
        prc.setDefaultRecipe(defaultRecipe);
        prc.getRecipes().putAll(recipes);

        ToveFileStorer fileStorer = fileLoaderFactory.createStorer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            fileStorer.store(baos, prc, new Element("project"));
            return new FixedPulseFileProvider(new String(baos.toByteArray()));
        }
        finally
        {
            IOUtils.close(baos);
        }
    }

    public String getDefaultRecipe()
    {
        return defaultRecipe;
    }

    public void setDefaultRecipe(String defaultRecipe)
    {
        this.defaultRecipe = defaultRecipe;
    }

    public Map<String, RecipeConfiguration> getRecipes()
    {
        return recipes;
    }

    public void setRecipes(Map<String, RecipeConfiguration> recipes)
    {
        this.recipes = recipes;
    }

    public void addRecipe(RecipeConfiguration recipe)
    {
        recipes.put(recipe.getName(), recipe);
    }
    
    public void setFileLoaderFactory(PulseFileLoaderFactory fileLoaderFactory)
    {
        this.fileLoaderFactory = fileLoaderFactory;
    }
}
