package com.zutubi.pulse.core.scm.cvs.client;

import com.zutubi.pulse.core.scm.cvs.CvsTestUtils;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import org.netbeans.lib.cvsclient.CVSRoot;

public class CvsCore_1_11_17_Test extends PulseTestCase
{
    private CvsCore cvs;

    protected void setUp() throws Exception
    {
        super.setUp();

        String password = CvsTestUtils.getPassword("cvs-1.11.17");
        assertNotNull(password);

        cvs = new CvsCore();
        cvs.setRoot(CVSRoot.parse(":ext:cvs-1.11.17:"+password+"@zutubi.com:/cvsroots/cvs-1.11.17"));
    }

    public void testVersion() throws ScmException
    {
        assertEquals("1.11.17", cvs.version());
    }
}
