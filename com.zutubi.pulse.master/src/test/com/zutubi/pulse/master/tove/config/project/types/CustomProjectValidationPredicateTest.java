package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.FileLoaderTestBase;
import com.zutubi.pulse.core.PulseScope;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;

public class CustomProjectValidationPredicateTest extends FileLoaderTestBase
{
    public void testCustomProjectValidation() throws Exception
    {
        ProjectRecipesConfiguration prc = new ProjectRecipesConfiguration();
        loader.load(getInput("customValidation", "xml"), prc, new PulseScope(), new CustomProjectValidationPredicate());
        assertNotNull(prc.getRecipes().get("bar"));
    }
}
