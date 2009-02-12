package com.zutubi.pulse.core.scm.cvs.client;

import com.zutubi.pulse.core.scm.cvs.CvsTestUtils;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import org.netbeans.lib.cvsclient.CVSRoot;

public class CvsCore_1_12_9_Test extends PulseTestCase
{
    private CvsCore cvs;

    protected void setUp() throws Exception
    {
        super.setUp();

        String password = CvsTestUtils.getPassword("cvs-1.12.9");
        assertNotNull(password);

        cvs = new CvsCore();
        cvs.setRoot(CVSRoot.parse(":ext:cvs-1.12.9:"+password+"@zutubi.com:/cvsroots/default"));
    }

    public void testVersion() throws ScmException
    {
        assertEquals("1.12.9", cvs.version());
    }
}