package com.zutubi.pulse.core.upgrade;

import com.zutubi.pulse.core.test.api.PulseTestCase;
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

    public void testOrderingPreserved() throws IOException, ParsingException
    {
        expectedOutputHelper();
    }

    public void testWhitespacePreserved() throws IOException, ParsingException
    {
        expectedOutputHelper(false);
    }

    public void testUTF8BOM() throws IOException, ParsingException
    {
        // This is just to check the parser itself does not throw an exception.
        PulseFileToToveFile.convert(getInput(EXTENSION_XML));
    }

    private void expectedOutputHelper() throws IOException, ParsingException
    {
        expectedOutputHelper(true);
    }

    private void expectedOutputHelper(boolean stripWhitespace) throws IOException, ParsingException
    {
        String in = readInputFully(getName() + ".in", EXTENSION_XML);
        String out = PulseFileToToveFile.convert(in);
        String expected = readInputFully(getName() + ".out", EXTENSION_XML);
        if (stripWhitespace)
        {
            out = stripWhitespace(out);
            expected = stripWhitespace(expected);
        }
        else
        {
            out = normaliseLineEndings(out);
            expected = normaliseLineEndings(expected);
        }
        assertEquals(expected, out);
    }

    private String stripWhitespace(String out)
    {
        return out.replaceAll("\\s+", "");
    }

    private String normaliseLineEndings(String s)
    {
        return s.replaceAll("\\r\\n", "\n");
    }
}
