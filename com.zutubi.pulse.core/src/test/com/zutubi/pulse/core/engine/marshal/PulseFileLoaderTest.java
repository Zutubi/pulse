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
