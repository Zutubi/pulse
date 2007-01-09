package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;

import java.io.IOException;

/**
 * Tests for RegexPostProcessor.
 */
public class MavenPostProcessorTest extends PostProcessorTestBase
{
    private MavenPostProcessor pp;

    public void setUp() throws IOException
    {
        super.setUp();
        pp = new MavenPostProcessor();
    }

    public void tearDown()
    {
        super.tearDown();
        pp = null;
    }

    public void testSuccess() throws Exception
    {
        CommandResult result = createAndProcessArtifact("success", pp);
        assertTrue(result.succeeded());
        assertEquals(0, artifact.getFeatures().size());
    }

    public void testCompilerError() throws Exception
    {
        createAndProcessArtifact("compilererror", pp);
        assertErrors("\n" +
                "BUILD FAILED\n" +
                "File...... C:\\Documents and Settings\\daniel\\.maven\\cache\\maven-java-plugin-1.5\\plugin.jelly\n" +
                "Element... ant:javac\n" +
                "Line...... 63\n" +
                "Column.... 48\n" +
                "Compile failed; see the compiler error output for details.\n" +
                "Total time: 1 seconds");
    }

    public void testTestFailure() throws Exception
    {
        createAndProcessArtifact("testfailure", pp);
        assertErrors(
                "    [junit] Running SimpleTest\n" +
                "    [junit] Tests run: 1, Failures: 1, Errors: 0, Time elapsed: 0.015 sec",

                "\n" +
                "BUILD FAILED\n"+
                "File...... C:\\Documents and Settings\\daniel\\.maven\\cache\\maven-test-plugin-1.6.2\\plugin.jelly\n"+
                "Element... fail\n"+
                "Line...... 181\n"+
                "Column.... 54\n"+
                "There were test failures.\n"+
                "Total time: 2 seconds");
    }
}

