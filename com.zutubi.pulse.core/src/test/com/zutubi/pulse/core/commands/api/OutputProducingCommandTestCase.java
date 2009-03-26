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
