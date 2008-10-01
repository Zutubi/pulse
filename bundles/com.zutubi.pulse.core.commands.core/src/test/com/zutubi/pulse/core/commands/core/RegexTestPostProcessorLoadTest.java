package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.FileLoaderTestBase;
import com.zutubi.pulse.core.PulseException;

/**
 */
public class RegexTestPostProcessorLoadTest extends FileLoaderTestBase
{
    public void setUp() throws Exception
    {
        super.setUp();

        loader.register("regex-test.pp", RegexTestPostProcessor.class);
    }

    public void testStandard() throws PulseException
    {
        RegexTestPostProcessor pp = referenceHelper("tests.pp");
        assertEquals("tests.pp", pp.getName());
        assertEquals("\\[(.*)\\].*E[S|D]T:(.*)", pp.getRegex().trim());
        assertEquals(1, pp.getStatusGroup());
        assertEquals(2, pp.getNameGroup());
        assertEquals(0, pp.getDetailsGroup());
        assertEquals("PASS", pp.getPassStatus());
        assertEquals("FAIL", pp.getFailureStatus());
        assertEquals(RegexTestPostProcessor.Resolution.APPEND, pp.getResolveConflicts());
    }
}
