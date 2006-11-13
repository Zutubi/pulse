package com.zutubi.pulse.core;

/**
 */
public class Maven2CommandLoadTest extends FileLoaderTestBase
{
    public void testEmpty() throws PulseException
    {
        Maven2Command command = commandHelper("empty");
        assertNull(command.getGoals());
    }

    public void testGoals() throws PulseException
    {
        Maven2Command command = commandHelper("goals");
        assertEquals("compile test", command.getGoals());
    }
}
