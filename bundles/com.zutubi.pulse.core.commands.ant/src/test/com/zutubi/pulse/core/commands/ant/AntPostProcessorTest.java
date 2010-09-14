package com.zutubi.pulse.core.commands.ant;

import com.zutubi.pulse.core.commands.core.RegexPostProcessor;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.postprocessors.PostProcessorTestBase;

import java.io.IOException;

/**
 * Tests for RegexPostProcessor.
 */
public class AntPostProcessorTest extends PostProcessorTestBase
{
    private RegexPostProcessor pp;

    public void setUp() throws IOException
    {
        pp = new RegexPostProcessor(new AntPostProcessorConfiguration());
        super.setUp();
    }

    public void testSuccess() throws Exception
    {
        CommandResult result = createAndProcessArtifact("success", pp);
        assertTrue(result.succeeded());
        assertEquals(0, artifact.getFeatures().size());
    }

    public void testNoBuildFile() throws Exception
    {
        createAndProcessArtifact("nobuild", pp);
        assertErrors("Buildfile: nobuild.xml does not exist!\nBuild failed");
    }

    public void testSyntaxError() throws Exception
    {
        createAndProcessArtifact("syntaxError", pp);
        assertErrors("Buildfile: build.xml\n" +
                "\n" +
                "BUILD FAILED\n" +
                "/home/jsankey/svn/oro/trunk/build.xml:-1: Element type \"attribute\" must be followed by either attribute specifications, \">\" or \"/>\".\n" +
                "\n" +
                "Total time: 0 seconds");
    }

    public void testJavacError() throws Exception
    {
        createAndProcessArtifact("javacError", pp);
        assertErrors("\n" +
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
                "Total time: 1 second");
    }

    public void testJavacWarning() throws Exception
    {
        createAndProcessArtifact("javacWarning", pp);
        assertWarnings("compile:\n" +
                "[mkdir] Created dir: /usr/pulse/data/recipes/851971/base/webwork/build/java\n" +
                "[javac] Compiling 476 source files to /usr/pulse/data/recipes/851971/base/webwork/build/java\n" +
                "[javac] /usr/pulse/data/recipes/851971/base/webwork/src/java/com/opensymphony/webwork/config/DelegatingConfiguration.java:17: warning: unmappable character for encoding UTF8\n" +
                "[javac] * @author Rickard �berg\n" +
                "[javac] ^");
    }
}
