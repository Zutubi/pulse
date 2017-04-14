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

package com.zutubi.pulse.core.commands.make;

import com.zutubi.pulse.core.commands.core.EnvironmentConfiguration;
import com.zutubi.pulse.core.commands.core.ExecutableCommandTestCase;
import com.zutubi.pulse.core.commands.core.NamedArgumentCommand;

import java.io.File;
import java.io.IOException;

public class MakeCommandTest extends ExecutableCommandTestCase
{
    public void testBasicDefault() throws Exception
    {
        copyMakefile("basic");
        MakeCommandConfiguration command = new MakeCommandConfiguration();
        successRun(command, "build target");
    }

    public void testBasicTargets() throws Exception
    {
        copyMakefile("basic");
        MakeCommandConfiguration command = new MakeCommandConfiguration();
        command.setTargets("build test");
        successRun(command, "build target", "test target");
    }

    public void testDoubleSpaceTargets() throws Exception
    {
        copyMakefile("basic");
        MakeCommandConfiguration command = new MakeCommandConfiguration();
        command.setTargets("build  test");
        successRun(command, "build target", "test target");
    }

    public void testEnvironment() throws Exception
    {
        copyMakefile("basic");
        MakeCommandConfiguration command = new MakeCommandConfiguration();
        command.setTargets("environment");
        command.getEnvironments().add(new EnvironmentConfiguration("TEST_ENV_VAR", "test variable value"));
        successRun(command, "test variable value");
    }

    public void testExplicitBuildfile() throws Exception
    {
        copyMakefile("basic", "custom.makefile");
        MakeCommandConfiguration command = new MakeCommandConfiguration();
        command.setMakefile("custom.makefile");
        successRun(command, "build target");
    }

    public void testExplicitArguments() throws Exception
    {
        copyMakefile("basic", "custom.makefile");
        MakeCommandConfiguration command = new MakeCommandConfiguration();
        command.setMakefile("custom.makefile");
        command.setTargets("build");
        command.setArgs("test");
        successRun(command, "build target", "test target");
    }

    public void testRunNoBuildFile() throws Exception
    {
        MakeCommandConfiguration command = new MakeCommandConfiguration();
        failedRun(command, "No targets specified and no makefile found");
    }

    public void testRunNonExistantBuildFile() throws Exception
    {
        MakeCommandConfiguration command = new MakeCommandConfiguration();
        command.setMakefile("nope.makefile");
        failedRun(command, "make: nope.makefile: No such file or directory");
    }

    private File copyMakefile(String name) throws IOException
    {
        return copyMakefile(name, "Makefile");
    }

    private File copyMakefile(String name, String toName) throws IOException
    {
        return copyBuildFile(name, "txt", toName);
    }

    private void successRun(MakeCommandConfiguration configuration, String... contents) throws Exception
    {
        successRun(new NamedArgumentCommand(configuration), contents);
    }

    private void failedRun(MakeCommandConfiguration configuration, String... contents) throws Exception
    {
        failedRun(new NamedArgumentCommand(configuration), contents);
    }
}
