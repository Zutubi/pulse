// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   VersionCommandTest.java

package com.zutubi.pulse.scm.cvs.client.commands;

import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.cvs.client.CvsClient;
import com.zutubi.pulse.test.PulseTestCase;
import java.io.File;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;

// Referenced classes of package com.zutubi.pulse.scm.cvs.client.commands:
//            VersionCommand

public class VersionCommandTest extends PulseTestCase
{

    public VersionCommandTest()
    {
    }

    public VersionCommandTest(String name)
    {
        super(name);
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();
        File repositoryRoot = new File(getPulseRoot(), "server-core/src/test/com/zutubi/pulse/scm/cvs/repository");
        String cvsRoot = (new StringBuilder()).append(":local:").append(repositoryRoot.getCanonicalPath()).toString();
        client = new CvsClient();
        client.setRoot(CVSRoot.parse(cvsRoot));
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    public void testVersionCommand()
        throws SCMException, CommandException, AuthenticationException
    {
        VersionCommand command = new VersionCommand();
        client.executeCommand(command, null, null);
        String serverVersion = command.getVersion();
        assertNotNull(serverVersion);
        assertTrue((new StringBuilder()).append(serverVersion).append(" does not contain 'CVS'.").toString(), serverVersion.contains("CVS"));
    }

    private CvsClient client;
}
