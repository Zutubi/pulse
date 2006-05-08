/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.test.PulseTestCase;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Tests for RegexPostProcessor.
 */
public class Maven2PostProcessorTest extends PostProcessorTestBase
{
    private Maven2PostProcessor pp;
    private File tempDir;


    public void setUp() throws IOException
    {
        pp = new Maven2PostProcessor();
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
        CommandResult result = createAndProcessArtifact("success", pp);
        assertTrue(result.succeeded());
        assertEquals(0, artifact.getFeatures().size());
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

    public void testNoTarget() throws Exception
    {
        createAndProcessArtifact("notarget", pp);
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
        createAndProcessArtifact("compilererror", pp);
        assertErrors("[INFO] ----------------------------------------------------------------------------\n" +
                "[ERROR] BUILD FAILURE\n" +
                "[INFO] ----------------------------------------------------------------------------\n" +
                "[INFO] Compilation failure\n" +
                "\n" +
                "base.dir/src/main/java/com/zutubi/maven2/test/App.java:[12,4] ';' expected\n" +
                "\n" +
                "");
    }

    public void testTestFailure() throws Exception
    {
        createAndProcessArtifact("testfailure", pp);
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
    }
}
