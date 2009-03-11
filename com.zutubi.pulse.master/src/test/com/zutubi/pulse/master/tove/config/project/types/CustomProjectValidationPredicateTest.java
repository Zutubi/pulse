package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.*;

public class CustomProjectValidationPredicateTest extends FileLoaderTestBase
{
    public void testCustomProjectValidation() throws Exception
    {
        PulseFile pulseFile = new PulseFile();
        loader.load(getInput("customValidation", "xml"), pulseFile, new PulseScope(), new ImportingNotSupportedFileResolver(), new FileResourceRepository(), new CustomProjectValidationPredicate());
        assertNotNull(pulseFile.getRecipe("bar"));
    }
}
