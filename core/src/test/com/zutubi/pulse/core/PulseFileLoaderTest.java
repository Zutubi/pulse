package com.zutubi.pulse.core;

import com.zutubi.pulse.model.ResourceRequirement;
import com.zutubi.pulse.util.IOUtils;

import java.util.List;

/**
 */
public class PulseFileLoaderTest extends FileLoaderTestBase
{
    public void testLoaderRequireResources() throws Exception
    {
        List<ResourceRequirement> requriements = loader.loadRequiredResources(IOUtils.inputStreamToString(getInput("requiredResources")), null);

        assertEquals(2, requriements.size());
        assertEquals("noversion", requriements.get(0).getResource());
        assertNull(requriements.get(0).getVersion());
        assertEquals("withversion", requriements.get(1).getResource());
        assertEquals("1", requriements.get(1).getVersion());
    }
}
