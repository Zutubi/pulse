/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core;

/**
 */
public class Maven2CommandLoadTest extends FileLoaderTestBase
{
    public void testEmpty() throws PulseException
    {
        Maven2Command command = commandHelper("empty");
        assertEquals("test", command.getTargets());
    }

    public void testTargets() throws PulseException
    {
        Maven2Command command = commandHelper("targets");
        assertEquals("compile test", command.getTargets());
    }
}
