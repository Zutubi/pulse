package com.zutubi.pulse.core.scm.api;

import com.zutubi.pulse.core.test.api.PulseTestCase;

public class PrefixPathFilterTest extends PulseTestCase
{
    public void testPathPrefix()
    {
        assertTrue(isSatisfied("/", "/something"));
        assertTrue(isSatisfied("/some", "/something"));
        assertTrue(isSatisfied("/some/thing", "/some/thing/path"));
        
        assertFalse(isSatisfied("something", "/something"));
    }

    public void testNormalisedPaths()
    {
        assertTrue(isSatisfied("/", "\\something"));
        assertTrue(isSatisfied("/some", "\\something"));
        assertTrue(isSatisfied("/some/thing", "\\some\\thing\\path"));

        assertTrue(isSatisfied("\\", "/something"));
        assertTrue(isSatisfied("\\some", "/something"));
        assertTrue(isSatisfied("\\some\\thing", "/some/thing/path"));
    }

    private boolean isSatisfied(String prefix, String path)
    {
        return new PrefixPathFilter(prefix).apply(path);
    }
}
