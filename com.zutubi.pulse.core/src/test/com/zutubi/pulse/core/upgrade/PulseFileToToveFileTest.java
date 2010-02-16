package com.zutubi.pulse.core.upgrade;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.io.IOUtils;
import nu.xom.ParsingException;

import java.io.IOException;

public class PulseFileToToveFileTest extends PulseTestCase
{
    private static final String EXTENSION_XML = "xml";

    public void testRemoveResource() throws IOException, ParsingException
    {
        expectedOutputHelper();
    }

    public void testRemoveResourceFromCommand() throws IOException, ParsingException
    {
        expectedOutputHelper();
    }

    public void testPullUpArtifacts() throws IOException, ParsingException
    {
        expectedOutputHelper();
    }

    public void testPushDownCommandName() throws IOException, ParsingException
    {
        expectedOutputHelper();
    }

    public void testRecipeVersion() throws IOException, ParsingException
    {
        expectedOutputHelper();
    }

    public void testUTF8BOM() throws IOException, ParsingException
    {
        // This is just to check the parser itself does not throw an exception.
        PulseFileToToveFile.convert(getInput(EXTENSION_XML));
    }

    private void expectedOutputHelper() throws IOException, ParsingException
    {
        String in = IOUtils.inputStreamToString(getInput(getName() + ".in", EXTENSION_XML));
        String out = PulseFileToToveFile.convert(in);
        out = stripWhitespace(out);
        String expected = IOUtils.inputStreamToString(getInput(getName() + ".out", EXTENSION_XML));
        expected = stripWhitespace(expected);
        assertEquals(expected, out);
    }

    private String stripWhitespace(String out)
    {
        return out.replaceAll("\\s+", "");
    }
}
