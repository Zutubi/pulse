package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.util.io.IOUtils;

import java.io.IOException;
import java.util.Arrays;

public class PulseFileLoaderTest extends FileLoaderTestBase
{
    public void testRecipeListing() throws PulseException, IOException
    {
        assertEquals(Arrays.asList("default", "two", "three"), loader.loadAvailableRecipes(getPulseFile(getName())));
    }

    private String getPulseFile(String name) throws IOException
    {
        return IOUtils.inputStreamToString(getInput(name, "xml"));
    }
}
