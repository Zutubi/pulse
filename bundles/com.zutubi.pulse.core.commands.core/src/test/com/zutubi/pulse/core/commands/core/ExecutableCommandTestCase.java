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

package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.commands.api.Command;
import com.zutubi.pulse.core.commands.api.OutputProducingCommandTestCase;
import com.zutubi.pulse.core.commands.api.TestCommandContext;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.io.IOException;

public abstract class ExecutableCommandTestCase extends OutputProducingCommandTestCase
{
    protected TestCommandContext failedRun(Command command, String ...contents) throws Exception
    {
        return stateRun(ResultState.FAILURE, command, contents);
    }

    protected TestCommandContext successRun(Command command, String ...contents) throws Exception
    {
        return stateRun(ResultState.SUCCESS, command, contents);
    }

    protected TestCommandContext stateRun(ResultState expectedState, Command command, String... contents) throws Exception
    {
        TestCommandContext context = runCommand(command);
        assertEquals(expectedState, context.getResultState());
        assertDefaultOutputContains(contents);
        return context;
    }

    protected File copyBuildFile(String name, String extension, String toName) throws IOException
    {
        File buildFile = copyInputToDirectory(name, extension, baseDir);
        FileSystemUtils.robustRename(buildFile, new File(baseDir, toName));
        return buildFile;
    }
}
