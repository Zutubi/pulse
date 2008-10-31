package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.FileLoaderTestBase;
import com.zutubi.pulse.core.FileResourceRepository;
import com.zutubi.pulse.core.PulseFile;
import com.zutubi.pulse.core.PulseScope;

public class CustomProjectValidationPredicateTest extends FileLoaderTestBase
{
    public void testCustomProjectValidation() throws Exception
    {
        PulseFile pulseFile = new PulseFile();
        loader.load(getInput("customValidation"), pulseFile, new PulseScope(), new FileResourceRepository(), new CustomProjectValidationPredicate());
        assertNotNull(pulseFile.getRecipe("bar"));
    }
}
