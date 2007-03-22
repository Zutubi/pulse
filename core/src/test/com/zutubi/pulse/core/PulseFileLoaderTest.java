package com.zutubi.pulse.core;

import com.zutubi.pulse.model.ResourceRequirement;
import com.zutubi.pulse.model.CustomProjectValidationPredicate;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.core.model.Property;

import java.util.List;

/**
 */
public class PulseFileLoaderTest extends FileLoaderTestBase
{
    public void setUp() throws Exception
    {
        super.setUp();

        loader.register("resource", ResourceReference.class);
    }

    public void testLoaderRequireResources() throws Exception
    {
        List<ResourceRequirement> requriements = loader.loadRequiredResources(IOUtils.inputStreamToString(getInput("requiredResources")), null);

        assertEquals(2, requriements.size());
        assertEquals("noversion", requriements.get(0).getResource());
        assertNull(requriements.get(0).getVersion());
        assertEquals("withversion", requriements.get(1).getResource());
        assertEquals("1", requriements.get(1).getVersion());
    }

    public void testCustomProjectValidation() throws Exception
    {
        PulseFile pulseFile = new PulseFile();
        loader.load(getInput("customValidation"), pulseFile, new Scope(), new FileResourceRepository(), new CustomProjectValidationPredicate());
        Recipe recipe = pulseFile.getRecipe("test");
        Command command = recipe.getCommand("bar");
        assertNotNull(command);
        List<Artifact> artifacts = command.getArtifacts();
        for(Artifact a: artifacts)
        {
            if(a.getName().equals("bar"))
            {
                return;
            }
        }
        fail("Did not find artifact named 'bar'");
    }
}
