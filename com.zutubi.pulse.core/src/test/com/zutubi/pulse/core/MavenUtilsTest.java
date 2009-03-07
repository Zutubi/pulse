package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.File;

public class MavenUtilsTest extends PulseTestCase
{
    private static final String EXTENSION_XML = "xml";
    private static final String ELEMENT_VERSION = "version";

    public void testExpectedFile() throws PulseException
    {
        assertEquals("1.0-SNAPSHOT", MavenUtils.extractVersion(getInputFile(EXTENSION_XML), ELEMENT_VERSION));
    }

    public void testNoVersionInFile() throws PulseException
    {
        assertNull(MavenUtils.extractVersion(getInputFile(EXTENSION_XML), ELEMENT_VERSION));
    }

    public void testNonParseableFile() throws PulseException
    {
        try
        {
            MavenUtils.extractVersion(getInputFile(EXTENSION_XML), ELEMENT_VERSION);
            fail("File should not be parseable");
        }
        catch (PulseException e)
        {
            assertThat(e.getMessage(), containsString("Unable to parse"));
        }
    }

    public void testFileDoesNotExist() throws PulseException
    {
        assertNull(MavenUtils.extractVersion(new File("there is no such file"), ELEMENT_VERSION));
    }
}
