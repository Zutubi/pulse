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

import com.zutubi.pulse.core.FileLoaderTestBase;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.marshal.ImportingNotSupportedFileResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class PulseFileLoaderTest extends FileLoaderTestBase
{
    public void testRecipeListing() throws PulseException, IOException
    {
        assertEquals(Arrays.asList("default", "two", "three"), loader.loadAvailableRecipes(getPulseFile(getName()), new ImportingNotSupportedFileResolver()));
    }
    
    public void testScopedProperties() throws PulseException, IOException
    {
        ProjectRecipesConfiguration recipesConfiguration = new ProjectRecipesConfiguration();
        loader.load(getInput(getName(), "xml"), recipesConfiguration, new ImportingNotSupportedFileResolver());
        Map<String,RecipeConfiguration> recipes = recipesConfiguration.getRecipes();
        assertEquals(2, recipes.size());
        assertTrue(recipes.containsKey("quux"));
        assertTrue(recipes.containsKey("bar"));
    }

    private String getPulseFile(String name) throws IOException
    {
        return readInputFully(name, "xml");
    }
}
