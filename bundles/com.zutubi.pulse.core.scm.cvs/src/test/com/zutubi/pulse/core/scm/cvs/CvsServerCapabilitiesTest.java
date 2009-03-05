package com.zutubi.pulse.core.scm.cvs;

import com.zutubi.pulse.core.test.api.PulseTestCase;

public class CvsServerCapabilitiesTest extends PulseTestCase
{
    protected void tearDown() throws Exception
    {
        System.clearProperty(CvsServerCapabilities.SUPPORT_REMOTE_LISTING);
        System.clearProperty(CvsServerCapabilities.SUPPORT_DATE_REVISION_ON_BRANCH);
        System.clearProperty(CvsServerCapabilities.SUPPORT_RLOG_SUPPRESS_HEADER);

        super.tearDown();
    }

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

    public void testUnexpectedFormat()
    {
        assertFalse(CvsServerCapabilities.supportsDateRevisionOnBranch("unexpected cvs version format"));
        assertFalse(CvsServerCapabilities.supportsRemoteListing("unexpected cvs version format"));
    }

    public void testUserOverride()
    {
        assertTrue(CvsServerCapabilities.supportsRemoteListing("1.12.1"));
        System.setProperty(CvsServerCapabilities.SUPPORT_REMOTE_LISTING, "false");
        assertFalse(CvsServerCapabilities.supportsRemoteListing("1.12.1"));
    }
}
