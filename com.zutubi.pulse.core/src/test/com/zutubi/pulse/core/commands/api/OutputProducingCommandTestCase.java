package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.Command;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.plugins.PostProcessorExtensionManager;

import java.io.IOException;

/**
 */
public abstract class OutputProducingCommandTestCase extends CommandTestCase
{
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
