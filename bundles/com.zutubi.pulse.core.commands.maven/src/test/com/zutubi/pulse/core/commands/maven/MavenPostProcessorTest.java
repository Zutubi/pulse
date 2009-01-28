package com.zutubi.pulse.core.commands.maven;

import com.zutubi.pulse.core.commands.core.PostProcessorGroup;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.postprocessors.DefaultPostProcessorFactory;
import com.zutubi.pulse.core.postprocessors.PostProcessorTestBase;
import com.zutubi.util.bean.DefaultObjectFactory;

import java.io.IOException;

public class MavenPostProcessorTest extends PostProcessorTestBase
{
    private PostProcessorGroup pp;

    public void setUp() throws IOException
    {
        super.setUp();

        DefaultPostProcessorFactory postProcessorFactory = new DefaultPostProcessorFactory();
        postProcessorFactory.setObjectFactory(new DefaultObjectFactory());

        pp = new PostProcessorGroup(new MavenPostProcessorConfiguration());
        pp.setPostProcessorFactory(postProcessorFactory);
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

