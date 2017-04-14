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

package com.zutubi.pulse.core.commands.maven3;

import com.zutubi.pulse.core.commands.api.TestCommandContext;
import com.zutubi.pulse.core.commands.core.ExecutableCommandTestCase;
import com.zutubi.pulse.core.engine.api.FieldScope;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.io.FileSystemUtils;

import java.io.IOException;

import static com.zutubi.util.CollectionUtils.asPair;

public class Maven3CommandTest extends ExecutableCommandTestCase
{
    public void testBasic() throws Exception
    {
        prepareBaseDir("basic");

        Maven3CommandConfiguration command = createCommandConfig();
        command.setGoals("compile");
        successRun(command, "(default-compile)", "BUILD SUCCESS");
    }

    public void testExtractVersion() throws Exception
    {
        prepareBaseDir("basic");

        Maven3CommandConfiguration command = createCommandConfig();
        command.setGoals("compile");
        TestCommandContext context = runCommand(new Maven3Command(command), createExecutionContext());
        assertEquals("1.0-SNAPSHOT", context.getCustomFields().get(asPair(FieldScope.BUILD, "maven.version")));
    }

    public void testNoTarget() throws Exception
    {
        prepareBaseDir("basic");

        Maven3CommandConfiguration command = createCommandConfig();
        failedRun(command, "BUILD FAILURE", "No goals have been specified for this build");
    }

    public void testMultiGoal() throws Exception
    {
        prepareBaseDir("basic");

        Maven3CommandConfiguration command = createCommandConfig();
        command.setGoals("compile test");
        successRun(command, "BUILD SUCCESS", "Running com.zutubi.maven3.test.AppTest",
                "(default-compile)", "(default-testCompile)", "(default-test)",
                "Tests run: 1, Failures: 0, Errors: 0, Skipped: 0");
    }

    public void testNoPOM() throws Exception
    {
        prepareBaseDir("nopom");

        Maven3CommandConfiguration command = createCommandConfig();
        command.setGoals("compile");
        failedRun(command, "BUILD FAILURE", "The goal you specified requires a project to execute but there is no POM in this directory");
    }

    public void testNonDefaultPOM() throws Exception
    {
        prepareBaseDir("nondefaultpom");

        Maven3CommandConfiguration command = createCommandConfig();
        command.setGoals("compile");
        command.setPomFile("blag/pom.xml");
        successRun(command, "(default-compile)", "BUILD SUCCESS");
    }

    public void testCompilerError() throws Exception
    {
        prepareBaseDir("compilererror");

        Maven3CommandConfiguration command = createCommandConfig();
        command.setGoals("compile");
        failedRun(command, "COMPILATION ERROR", "BUILD FAILURE", "(default-compile)");
    }

    public void testTestFailure() throws Exception
    {
        prepareBaseDir("testfailure");

        Maven3CommandConfiguration command = createCommandConfig();
        command.setGoals("test");
        failedRun(command, "BUILD FAILURE", "Tests run: 1, Failures: 1, Errors: 0, Skipped: 0");
    }

    private void prepareBaseDir(String name) throws IOException
    {
        FileSystemUtils.rmdir(baseDir);
        assertTrue(baseDir.mkdir());

        unzipInput(name, baseDir);
    }

    private Maven3CommandConfiguration createCommandConfig()
    {
        Maven3CommandConfiguration config = new Maven3CommandConfiguration();
        String m3Home = System.getenv("MAVEN3_HOME");
        assertNotNull("MAVEN3_HOME must be set to the path of the maven 3 installation", m3Home);
        config.setExe(m3Home + "/bin/mvn" + (SystemUtils.IS_WINDOWS ? ".bat" : ""));
        return config;
    }

    private TestCommandContext successRun(Maven3CommandConfiguration configuration, String... content) throws Exception
    {
        return successRun(new Maven3Command(configuration), content);
    }

    private TestCommandContext failedRun(Maven3CommandConfiguration configuration, String... content) throws Exception
    {
        TestCommandContext context = runCommand(new Maven3Command(configuration));
        if (!SystemUtils.IS_WINDOWS)
        {
            assertEquals(ResultState.FAILURE, context.getResultState());
        }
        assertDefaultOutputContains(content);
        return context;
    }
}
