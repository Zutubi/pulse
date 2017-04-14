/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.commands.maven2;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.postprocessors.DefaultPostProcessorFactory;
import com.zutubi.pulse.core.postprocessors.PostProcessorTestBase;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.bean.DefaultObjectFactory;

import java.io.IOException;

public class Maven2PostProcessorTest extends PostProcessorTestBase
{
    private Maven2PostProcessorConfiguration config;

    public void setUp() throws IOException
    {
        super.setUp();
        config = new Maven2PostProcessorConfiguration("maven2.pp");
    }

    private Maven2PostProcessor createProcessor()
    {
        DefaultPostProcessorFactory postProcessorFactory = new DefaultPostProcessorFactory();
        postProcessorFactory.setObjectFactory(new DefaultObjectFactory());

        Maven2PostProcessor pp = new Maven2PostProcessor(config);
        pp.setPostProcessorFactory(postProcessorFactory);
        return pp;
    }

    public void testSuccess() throws Exception
    {
        CommandResult result = createAndProcessArtifact("success", createProcessor());
        assertTrue(result.succeeded());
        assertEquals(0, artifact.getFeatures().size());
    }

    public void testWarnings() throws Exception
    {
        CommandResult result = createAndProcessArtifact("warnings", createProcessor());
        assertTrue(result.succeeded());
        assertWarnings("Compiling 1 source file to base.dir/target/classes\n" +
                "[WARNING] Removing: jar from forked lifecycle, to prevent recursive invocation.\n" +
                "[WARNING] Another warning\n" +
                "[INFO] ----------------------------------------------------------------------------\n" +
                "[INFO] BUILD SUCCESSFUL\n" +
                "[INFO] ----------------------------------------------------------------------------\n" +
                "[INFO] ----------------------------------------------------------------------------");
    }

    public void testSuppressAllWarnings() throws Exception
    {
        config.getSuppressedWarnings().add(".*");
        CommandResult result = createAndProcessArtifact("warnings", createProcessor());
        assertTrue(result.succeeded());
        assertEquals(0, artifact.getFeatures().size());
    }

    public void testSuppressWarning() throws Exception
    {
        // Turn off context because it makes it difficult to see the right
        // warning is suppressed.
        config.setLeadingContext(0);
        config.setTrailingContext(0);
        
        config.getSuppressedWarnings().add("jar from forked lifecycle");
        CommandResult result = createAndProcessArtifact("warnings", createProcessor());
        assertTrue(result.succeeded());
        assertWarnings("[WARNING] Another warning");
    }

    public void testNoPOM() throws Exception
    {
        createAndProcessArtifact("nopom", createProcessor());
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
        createAndProcessArtifact("nogoal", createProcessor());
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
        CommandResult result = createAndProcessArtifact("compilererror", createProcessor());
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
        CommandResult result = createAndProcessArtifact("fatalerror", createProcessor());
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
        CommandResult result = createAndProcessArtifact("testfailure", createProcessor());
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
        CommandResult result = createAndProcessArtifact("successfulerror", createProcessor());
        assertErrors("[INFO] Generate \"Continuous Integration\" report.\n" +
                "[ERROR] VM #displayTree: error : too few arguments to macro. Wanted 2 got 0\n" +
                "[ERROR] VM #menuItem: error : too few arguments to macro. Wanted 1 got 0\n" +
                "[INFO] Generate \"Dependencies\" report.\n" +
                "[INFO] Generate \"Issue Tracking\" report.\n" +
                "[INFO] Generate \"Project License\" report.\n" +
                "[INFO] Generate \"Mailing Lists\" report.\n" +
                "[INFO] Generate \"About\" report.\n" +
                "[INFO] Generate \"Project Summary\" report.");
        assertTrue(result.succeeded());
    }

    public void testSuppressError() throws Exception
    {
        config.getSuppressedErrors().add("too few arguments");
        CommandResult result = createAndProcessArtifact("successfulerror", createProcessor());
        assertEquals(0, artifact.getFeatures().size());
        assertTrue(result.succeeded());
    }
}
