/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.util.FileSystemUtils;
import com.zutubi.pulse.core.util.IOUtils;
import com.zutubi.pulse.test.PulseTestCase;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Tests for RegexPostProcessor.
 */
public class AntPostProcessorTest extends PulseTestCase
{
    private AntPostProcessor pp;
    private StoredFileArtifact artifact;
    private File tempDir;


    public void setUp() throws IOException
    {
        pp = new AntPostProcessor();
        tempDir = FileSystemUtils.createTempDirectory(getClass().getName(), "");
    }

    public void tearDown()
    {
        FileSystemUtils.removeDirectory(tempDir);
        artifact = null;
        pp = null;
    }

    public void testSuccess() throws Exception
    {
        CommandResult result = createAndProcessArtifact("success");
        assertTrue(result.succeeded());
        assertEquals(0, artifact.getFeatures().size());
    }

    public void testNoBuildFile() throws Exception
    {
        createAndProcessArtifact("nobuild");
        assertEquals(1, artifact.getFeatures(Feature.Level.ERROR).size());
        Feature feature = artifact.getFeatures(Feature.Level.ERROR).get(0);
        assertEquals("Buildfile: nobuild.xml does not exist!\nBuild failed", feature.getSummary());
    }

    public void testSyntaxError() throws Exception
    {
        createAndProcessArtifact("syntaxError");
        assertEquals(1, artifact.getFeatures(Feature.Level.ERROR).size());
        Feature feature = artifact.getFeatures(Feature.Level.ERROR).get(0);
        assertEquals("Buildfile: build.xml\n" +
                "\n" +
                "BUILD FAILED\n" +
                "/home/jsankey/svn/oro/trunk/build.xml:-1: Element type \"attribute\" must be followed by either attribute specifications, \">\" or \"/>\".\n" +
                "\n" +
                "Total time: 0 seconds",
                feature.getSummary());
    }

    public void testJavacError() throws Exception
    {
        createAndProcessArtifact("javacError");
        assertEquals(1, artifact.getFeatures(Feature.Level.ERROR).size());
        List<Feature> features = artifact.getFeatures(Feature.Level.ERROR);
        Feature feature = features.get(0);
        assertEquals("\n" +
                "prepare:\n" +
                "\n" +
                "compile:\n" +
                "    [javac] Compiling 1 source file to /home/jsankey/svn/oro/trunk/classes\n" +
                "    [javac] /home/jsankey/svn/oro/trunk/src/java/org/apache/oro/io/RegexFilenameFilter.java:41: ';' expected\n" +
                "    [javac]   PatternMatcher _matcher;\n" +
                "    [javac]   ^\n" +
                "    [javac] 1 error\n" +
                "\n" +
                "BUILD FAILED\n" +
                "/home/jsankey/svn/oro/trunk/build.xml:127: Compile failed; see the compiler error output for details.\n" +
                "\n" +
                "Total time: 1 second",
                feature.getSummary());
    }

    private CommandResult createAndProcessArtifact(String name) throws Exception
    {
        createArtifact(name);
        CommandResult commandResult = new CommandResult("test");
        commandResult.commence(tempDir);
        pp.process(tempDir, artifact, commandResult);
        commandResult.complete();
        return commandResult;
    }

    public void createArtifact(String name) throws Exception
    {
        URL url = getInputURL(name, ".txt");
        File fromFile = new File(url.toURI());
        File toFile = new File(tempDir, fromFile.getName());
        IOUtils.copyFile(fromFile, toFile);
        artifact = new StoredFileArtifact(toFile.getName());
    }
}
