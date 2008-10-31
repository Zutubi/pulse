package com.zutubi.pulse.core.scm.cvs.config;

import com.zutubi.pulse.core.test.PulseTestCase;

/**
 */
public class CvsConfigurationTest extends PulseTestCase
{
    public void testGetPrevious()
    {
        CvsConfiguration conf = new CvsConfiguration();
        assertEquals("1.3", conf.getPreviousRevision("1.4"));
    }
}
