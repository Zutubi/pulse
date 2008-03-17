package com.zutubi.pulse.core;

import com.zutubi.pulse.model.CustomProjectValidationPredicate;
import com.zutubi.pulse.model.ResourceRequirement;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.IOUtils;

import java.io.IOException;
import java.util.List;

/**
 */
public class PulseFileLoaderTest extends PulseTestCase
{
    PulseFileLoader loader;

    public void setUp()
    {
        loader = new PulseFileLoader();
        loader.setObjectFactory(new ObjectFactory());
    }

    protected void tearDown() throws Exception
    {
        loader = null;
    }

    public void testLoaderRequireResources() throws Exception
    {
        List<ResourceRequirement> requirements = loader.loadRequiredResources(getPulseFile("requiredResources"), null);

        assertEquals(3, requirements.size());
        assertEquals("noversion", requirements.get(0).getResource());
        assertNull(requirements.get(0).getVersion());
        assertEquals("withversion", requirements.get(1).getResource());
        assertEquals("1", requirements.get(1).getVersion());

        assertEquals("explicitlyrequired", requirements.get(2).getResource());
        assertNull(requirements.get(2).getVersion());
    }

    public void testLoaderNestedRequiredResource() throws Exception
    {
        List<ResourceRequirement> requirements = loader.loadRequiredResources(getPulseFile("requiredResources"), "nested");
        assertEquals(1, requirements.size());
        assertEquals("nested", requirements.get(0).getResource());

    }

    public void testLoaderRequiredResourceUnresolvableProperties() throws Exception
    {
        List<ResourceRequirement> requirements = loader.loadRequiredResources(getPulseFile("requiredResources"), "unresolvables");
        assertEquals(1, requirements.size());
        assertEquals("un", requirements.get(0).getResource());
    }

    public void testLoaderRequiredResourceNoName() throws Exception
    {
        try
        {
            loader.loadRequiredResources(getPulseFile("requiredResources"), "noname");
            fail();
        }
        catch (Exception e)
        {
            assertTrue(e.getMessage().contains("Required attribute name not specified"));
        }
    }

    private String getPulseFile(String name) throws IOException
    {
        return IOUtils.inputStreamToString(getInput(name));
    }

    public void testCustomProjectValidation() throws Exception
    {
        PulseFile pulseFile = new PulseFile();
        loader.load(getInput("customValidation"), pulseFile, new Scope(), new FileResourceRepository(), new CustomProjectValidationPredicate());
        Recipe recipe = pulseFile.getRecipe("test");
        Command command = recipe.getCommand("bar");
        assertNotNull(command);
        assertTrue(command.getArtifactNames().contains("bar"));
    }
}
