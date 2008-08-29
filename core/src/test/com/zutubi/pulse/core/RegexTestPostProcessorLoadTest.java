package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.TestSuiteResult;

/**
 */
public class RegexTestPostProcessorLoadTest extends FileLoaderTestBase
{
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
        assertEquals(TestSuiteResult.Resolution.APPEND, pp.getResolveConflicts());
    }
}
