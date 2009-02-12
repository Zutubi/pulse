package com.zutubi.pulse.core.scm.cvs;

import com.zutubi.pulse.core.test.api.PulseTestCase;

public class CvsServerCapabilitiesTest extends PulseTestCase
{
    public void testRemoteListing()
    {
        assertTrue(CvsServerCapabilities.supportsRemoteListing("1.12.1"));
        assertFalse(CvsServerCapabilities.supportsRemoteListing("1.11.1"));
    }

    public void testDateRevisionOnBranch()
    {
        assertTrue(CvsServerCapabilities.supportsDateRevisionOnBranch("1.12.12"));
        assertFalse(CvsServerCapabilities.supportsDateRevisionOnBranch("1.11.1"));
        assertFalse(CvsServerCapabilities.supportsDateRevisionOnBranch("1.12.9"));
        assertFalse(CvsServerCapabilities.supportsDateRevisionOnBranch("1.12"));
    }
}
