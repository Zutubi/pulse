package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.postprocessors.PostProcessorTestBase;
import com.zutubi.pulse.util.SystemUtils;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Tests for RegexPostProcessor.
 */
public class Maven2PostProcessorTest extends PostProcessorTestBase
{
    private Maven2PostProcessor pp;

    public void setUp() throws IOException
    {
        pp = new Maven2PostProcessor();
        super.setUp();
    }

    public void tearDown()
    {
        pp = null;
        super.tearDown();
    }

    public void testSuccess() throws Exception
    {
        CommandResult result = createAndProcessArtifact("success", pp);
        assertTrue(result.succeeded());
        assertEquals(0, artifact.getFeatures().size());
    }

    public void testWarnings() throws Exception
    {
        CommandResult result = createAndProcessArtifact("warnings", pp);
        assertTrue(result.succeeded());
        assertWarnings("Compiling 1 source file to base.dir/target/classes\n" +
                       "[WARNING] Removing: jar from forked lifecycle, to prevent recursive invocation.\n" +
                       "[WARNING] Another warning\n" +
                       "[INFO] ----------------------------------------------------------------------------");
    }

    public void testSuppressAllWarnings() throws Exception
    {
        pp.addSuppressWarning(new ExpressionElement(Pattern.compile(".*")));
        CommandResult result = createAndProcessArtifact("warnings", pp);
        assertTrue(result.succeeded());
        assertEquals(0, artifact.getFeatures().size());
    }

    public void testSuppressWarning() throws Exception
    {
        pp.addSuppressWarning(new ExpressionElement(Pattern.compile(".*jar from forked lifecycle.*")));
        CommandResult result = createAndProcessArtifact("warnings", pp);
        assertTrue(result.succeeded());
        assertWarnings("[WARNING] Removing: jar from forked lifecycle, to prevent recursive invocation.\n" +
                       "[WARNING] Another warning\n" +
                       "[INFO] ----------------------------------------------------------------------------");
    }

    public void testNoPOM() throws Exception
    {
        createAndProcessArtifact("nopom", pp);
        assertErrors("[INFO] ----------------------------------------------------------------------------\n" +
                "[ERROR] BUILD ERROR\n" +
                "[INFO] ----------------------------------------------------------------------------\n" +
                "[INFO] Cannot execute mojo: resources. It requires a project, but the build is not using one.\n" +
                "[INFO] ----------------------------------------------------------------------------\n" +
                "[INFO] For more information, run Maven with the -e switch\n" +
                "[INFO] ----------------------------------------------------------------------------\n" +
                "[INFO] ----------------------------------------------------------------------------");
    }

    public void testNoGoal() throws Exception
    {
        createAndProcessArtifact("nogoal", pp);
        assertErrors("[INFO] ----------------------------------------------------------------------------\n" +
                "[ERROR] BUILD FAILURE\n" +
                "[INFO] ----------------------------------------------------------------------------\n" +
                "[INFO] You must specify at least one goal. Try 'install'\n" +
                "[INFO] ----------------------------------------------------------------------------\n" +
                "[INFO] For more information, run Maven with the -e switch\n" +
                "[INFO] ----------------------------------------------------------------------------\n" +
                "[INFO] ----------------------------------------------------------------------------");
    }

    public void testCompilerError() throws Exception
    {
        CommandResult result = createAndProcessArtifact("compilererror", pp);
        assertErrors("[INFO] ----------------------------------------------------------------------------\n" +
                "[ERROR] BUILD FAILURE\n" +
                "[INFO] ----------------------------------------------------------------------------\n" +
                "[INFO] Compilation failure\n" +
                "\n" +
                "base.dir/src/main/java/com/zutubi/maven2/test/App.java:[12,4] ';' expected\n" +
                "\n");

        if(SystemUtils.IS_WINDOWS)
        {
            assertTrue(result.failed());
        }
        else
        {
            assertTrue(result.succeeded());
        }
    }

    public void testFatalError() throws Exception
    {
        CommandResult result = createAndProcessArtifact("fatalerror", pp);
        assertErrors("[INFO] ------------------------------------------------------------------------\n" +
                "[ERROR] FATAL ERROR\n" +
                "[INFO] ------------------------------------------------------------------------\n" +
                "[INFO] Error building POM (may not be this project's POM).\n" +
                "\n" +
                "\n" +
                "Project ID: unknown\n");

        if(SystemUtils.IS_WINDOWS)
        {
            assertTrue(result.failed());
        }
        else
        {
            assertTrue(result.succeeded());
        }
    }

    public void testTestFailure() throws Exception
    {
        CommandResult result = createAndProcessArtifact("testfailure", pp);
        assertErrors("[surefire] Running com.zutubi.maven2.test.AppTest\n" +
                "[surefire] Tests run: 1, Failures: 1, Errors: 0, Time elapsed: x sec <<<<<<<< FAILURE !! ",

                "[INFO] ----------------------------------------------------------------------------\n" +
                "[ERROR] BUILD ERROR\n" +
                "[INFO] ----------------------------------------------------------------------------\n" +
                "[INFO] There are test failures.\n" +
                "[INFO] ----------------------------------------------------------------------------\n" +
                "[INFO] For more information, run Maven with the -e switch\n" +
                "[INFO] ----------------------------------------------------------------------------\n" +
                "[INFO] ----------------------------------------------------------------------------");

        if(SystemUtils.IS_WINDOWS)
        {
            assertTrue(result.failed());
        }
        else
        {
            assertTrue(result.succeeded());
        }
    }

    public void testSuccessfulError() throws Exception
    {
        CommandResult result = createAndProcessArtifact("successfulerror", pp);
        assertErrors("[INFO] Generate \"Continuous Integration\" report.\n" +
                "[ERROR] VM #displayTree: error : too few arguments to macro. Wanted 2 got 0\n" +
                "[ERROR] VM #menuItem: error : too few arguments to macro. Wanted 1 got 0\n" +
                "[INFO] Generate \"Dependencies\" report.");
        assertTrue(result.succeeded());
    }

    public void testSuppressError() throws Exception
    {
        pp.addSuppressError(new ExpressionElement(Pattern.compile(".*too few arguments.*")));
        CommandResult result = createAndProcessArtifact("successfulerror", pp);
        assertEquals(0, artifact.getFeatures().size());
        assertTrue(result.succeeded());
    }
}
