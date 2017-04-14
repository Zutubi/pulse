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

import com.zutubi.pulse.core.commands.api.TestCommandContext;
import com.zutubi.pulse.core.commands.core.ExecutableCommandTestCase;
import com.zutubi.pulse.core.engine.api.FieldScope;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.util.SystemUtils;

import java.io.File;
import java.io.IOException;

import static com.zutubi.util.CollectionUtils.asPair;

public class MavenCommandTest extends ExecutableCommandTestCase
{
    public void testNoBuildFileNoTargets() throws Exception
    {
        MavenCommandConfiguration command = new MavenCommandConfiguration();
        successRun(command, "BUILD SUCCESSFUL", "Total time", "_Apache_", "v. 1.1");
    }

    public void testDefaultTarget() throws Exception
    {
        copyMavenFile("basic");
        MavenCommandConfiguration command = new MavenCommandConfiguration();
        successRun(command, "BUILD SUCCESSFUL", "build target", "_Apache_", "v. 1.1");
    }

    public void testExtractVersion() throws Exception
    {
        copyMavenFile("basic", "project.xml");
        MavenCommandConfiguration command = new MavenCommandConfiguration();
        TestCommandContext context = runCommand(new MavenCommand(command), createExecutionContext());
        assertEquals("1.0-SNAPSHOT", context.getCustomFields().get(asPair(FieldScope.BUILD, "maven.version")));
    }

    public void testRunSpecificTarget() throws Exception
    {
        copyMavenFile("basic");
        MavenCommandConfiguration command = new MavenCommandConfiguration();
        command.setTargets("mytest");
        successRun(command, "BUILD SUCCESSFUL", "test target", "_Apache_", "v. 1.1");
    }

    public void testRunMultipleTargets() throws Exception
    {
        copyMavenFile("basic");
        MavenCommandConfiguration command = new MavenCommandConfiguration();
        command.setTargets("mybuild mytest");
        successRun(command, "BUILD SUCCESSFUL", "build target", "test target", "_Apache_", "v. 1.1");
    }

    public void testMissingTarget() throws Exception
    {
        copyMavenFile("basic");
        MavenCommandConfiguration command = new MavenCommandConfiguration();
        command.setTargets("missingTarget");

        TestCommandContext context = runCommand(new MavenCommand(command));
        // Windows batch files have unreliable exit codes, so we don't confirm
        // on Windows (where the post-processor fixes this).
        if (!SystemUtils.IS_WINDOWS)
        {
            assertEquals(ResultState.FAILURE, context.getResultState());
        }

        assertDefaultOutputContains("BUILD FAILED");
    }

    public void testExtraArgument() throws Exception
    {
        copyMavenFile("basic");
        MavenCommandConfiguration command = new MavenCommandConfiguration();
        command.setTargets("mybuild");
        command.setArgs("-X");
        successRun(command, "Loading plugin", "'maven-ejb-plugin", "build target", "_Apache_", "v. 1.1");
    }

    private File copyMavenFile(String name) throws IOException
    {
        return copyMavenFile(name, "maven.xml");
    }

    private File copyMavenFile(String name, String toName) throws IOException
    {
        return copyBuildFile(name, "xml", toName);
    }

    private void successRun(MavenCommandConfiguration configuration, String... content) throws Exception
    {
        successRun(new MavenCommand(configuration), content);
    }
}
