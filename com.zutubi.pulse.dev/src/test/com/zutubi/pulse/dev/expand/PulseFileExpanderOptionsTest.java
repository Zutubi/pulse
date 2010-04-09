package com.zutubi.pulse.dev.expand;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.util.List;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;

public class PulseFileExpanderOptionsTest extends PulseTestCase
{
    public void testStandardOptions() throws PulseException
    {
        PulseFileExpanderOptions options = new PulseFileExpanderOptions(
                "-r", "test.recipe",
                "-e", "test.resources",
                "test.file"
        );
        
        assertEquals("test.file", options.getPulseFile());
        assertEquals("test.recipe", options.getRecipe());
        assertEquals("test.resources", options.getResourcesFile());
    }

    public void testStandardLongOptions() throws PulseException
    {
        PulseFileExpanderOptions options = new PulseFileExpanderOptions(
                "--recipe", "test.recipe",
                "--resources-file", "test.resources",
                "test.file"
        );
        
        assertEquals("test.file", options.getPulseFile());
        assertEquals("test.recipe", options.getRecipe());
        assertEquals("test.resources", options.getResourcesFile());
    }

    public void testRequirements() throws PulseException
    {
        PulseFileExpanderOptions options = new PulseFileExpanderOptions(
                "-q", "r1",
                "--require", "r2/ver",
                "pulse.xml"
        );
        
        List<ResourceRequirement> requirements = options.getResourceRequirements();
        assertEquals(2, requirements.size());
        assertThat(requirements, hasItem(new ResourceRequirement("r1", false)));
        assertThat(requirements, hasItem(new ResourceRequirement("r2", "ver", false)));
    }
    
    public void testDefines() throws PulseException
    {
        PulseFileExpanderOptions options = new PulseFileExpanderOptions(
                "-d", "foo=bar",
                "--define", "baz=quux",
                "pulse.xml"
        );
        
        Properties defines = options.getDefines();
        assertEquals(2, defines.size());
        assertEquals("bar", defines.get("foo"));
        assertEquals("quux", defines.get("baz"));
    }
    
    public void testNoPulseFile()
    {
        try
        {
            new PulseFileExpanderOptions(new String[0]);
            fail("Should require pulse file");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("No pulse file specified."));
        }
    }
}
