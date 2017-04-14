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

package com.zutubi.pulse.core.commands.maven;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.postprocessors.DefaultPostProcessorFactory;
import com.zutubi.pulse.core.postprocessors.PostProcessorTestBase;
import com.zutubi.util.bean.DefaultObjectFactory;

import java.io.IOException;

public class MavenPostProcessorTest extends PostProcessorTestBase
{
    private MavenPostProcessor pp;

    public void setUp() throws IOException
    {
        super.setUp();

        DefaultPostProcessorFactory postProcessorFactory = new DefaultPostProcessorFactory();
        postProcessorFactory.setObjectFactory(new DefaultObjectFactory());

        pp = new MavenPostProcessor(new MavenPostProcessorConfiguration());
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

