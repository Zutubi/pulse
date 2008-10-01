package com.zutubi.pulse.core.scm.cvs.client.commands;

import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.scm.cvs.client.CvsCore;
import com.zutubi.pulse.core.test.PulseTestCase;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;

public class VersionCommandTest extends PulseTestCase
{
    public VersionCommandTest()
    {
    }

    public void testVersionCommandAgainst1_12_12() throws ScmException, CommandException, AuthenticationException
    {
        VersionCommand command = new VersionCommand();

        CvsCore core = new CvsCore();
        core.setRoot(CVSRoot.parse(":ext:cvs-1.12.12@cinnamonbob.com:/cvsroots/cvs-1.12.12"));
        core.setPassword("cvs-1.12.12");
        core.executeCommand(command, null, null);

        String serverVersion = command.getVersion();

        assertNotNull(serverVersion);
        assertEquals("Concurrent Versions System (CVS) 1.12.12 (client/server)", serverVersion);
    }

    public void testVersionCommandAgainst1_11_19() throws ScmException, CommandException, AuthenticationException
    {
        VersionCommand command = new VersionCommand();

        CvsCore core = new CvsCore();
        core.setRoot(CVSRoot.parse(":ext:cvstester@cinnamonbob.com:/cvsroot"));
        core.setPassword("cvs");
        core.executeCommand(command, null, null);

        String serverVersion = command.getVersion();

        assertNotNull(serverVersion);
        assertEquals("Concurrent Versions System (CVS) 1.11.19 (client/server)", serverVersion);
    }
}
