/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core;

/**
 */
public class Maven2PostProcessorLoadTest extends FileLoaderTestBase
{
    public void testEmpty() throws PulseException
    {
        Maven2PostProcessor pp = referenceHelper("empty");
        assertEquals(2, pp.size());
    }
}
