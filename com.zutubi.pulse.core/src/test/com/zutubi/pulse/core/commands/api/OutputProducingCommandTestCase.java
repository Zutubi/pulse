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

package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.plugins.PostProcessorExtensionManager;

import java.io.IOException;

/**
 * Helper base for implementing tests of {@link OutputProducingCommandSupport}
 * instances.
 */
public abstract class OutputProducingCommandTestCase extends CommandTestCase
{
    /**
     * Asserts that the output produced by the command contains the given
     * strings.
     *
     * @param contents the strings expected in the output
     * @throws IOException if there is an error reading the file
     */
    protected void assertDefaultOutputContains(String... contents) throws IOException
    {
        assertFileContains(OutputProducingCommandSupport.OUTPUT_NAME, OutputProducingCommandSupport.OUTPUT_FILE, contents);
    }

    @Override
    protected TestCommandContext runCommand(Command command, ExecutionContext context)
    {
        ((OutputProducingCommandSupport) command).setPostProcessorExtensionManager(new PostProcessorExtensionManager());
        return super.runCommand(command, context);
    }
}
