package com.zutubi.pulse.core.commands.api;

import java.io.IOException;

/**
 */
public abstract class OutputProducingCommandTestCase extends CommandTestCase
{
    protected void assertOutputContains(String... contents) throws IOException
    {
        assertFileContains(OutputProducingCommandSupport.OUTPUT_NAME, OutputProducingCommandSupport.OUTPUT_FILE, contents);
    }
}
