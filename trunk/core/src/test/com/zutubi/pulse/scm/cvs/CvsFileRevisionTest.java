package com.zutubi.pulse.scm.cvs;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.core.model.CvsFileRevision;

/**
 */
public class CvsFileRevisionTest extends PulseTestCase
{
    public void testGetPrevious()
    {
        CvsFileRevision r = new CvsFileRevision("1.4");
        assertEquals("1.3", r.getPrevious().getRevisionString());
    }
}
