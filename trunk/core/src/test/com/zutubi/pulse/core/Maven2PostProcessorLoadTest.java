package com.zutubi.pulse.core;

/**
 */
public class Maven2PostProcessorLoadTest extends FileLoaderTestBase
{
    public void testEmpty() throws PulseException
    {
        Maven2PostProcessor pp = referenceHelper("empty");
        assertEquals(3, pp.size());
    }

    public void testFailOnError() throws PulseException
    {
        Maven2PostProcessor pp = referenceHelper("fail");
        assertEquals(3, pp.size());
        assertTrue(((RegexPostProcessor)pp.get(1)).getFailOnError());
        assertTrue(((RegexPostProcessor)pp.get(2)).getFailOnError());
    }
}
