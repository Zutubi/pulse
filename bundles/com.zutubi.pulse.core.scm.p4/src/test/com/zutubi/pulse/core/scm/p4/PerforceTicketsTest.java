/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.scm.ScmContextImpl;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.api.ScmFeedbackAdapter;
import com.zutubi.pulse.core.scm.p4.config.PerforceConfiguration;

import java.io.File;

public class PerforceTicketsTest extends PerforceTestBase
{
    private static final String FILE_ANT_BUILD = "build.xml";

    private static final String USER = "ticketmaster";
    private static final String PASSWORD = "TicketMaster";
    private static final String WORKSPACE = "test";

    private PerforceConfiguration configuration;

    protected void setUp() throws Exception
    {
        super.setUp();

        deployPerforceServer("repo", P4D_PORT, 1, false);

        configuration = new PerforceConfiguration(getP4Port(), USER, PASSWORD, WORKSPACE);
        configuration.setUseTicketAuth(true);
    }

    public void testSimpleConnectionTest() throws ScmException
    {
        PerforceClient client = new PerforceClient(configuration, new PerforceWorkspaceManager());
        client.testConnection(createScmContext());
    }

    public void testConnectionTestWithoutTickets() throws ScmException
    {
        configuration.setUseTicketAuth(false);
        failedConnectTestHelper("Password not allowed at this server security level");
    }

    public void testConnectionTestWithBadPassword() throws ScmException
    {
        configuration.setPassword("wrong");
        failedConnectTestHelper("Password invalid");
    }

    private void failedConnectTestHelper(String expectedMessage) throws ScmException
    {
        try
        {
            PerforceClient client = new PerforceClient(configuration, new PerforceWorkspaceManager());
            client.testConnection(createScmContext());
            fail("Expecting authentication to fail");
        }
        catch (ScmException e)
        {
            assertTrue("'" + e.getMessage() + "' does not contain '" + expectedMessage + "'", e.getMessage().contains(expectedMessage));
        }
    }

    public void testMultipleInteractions() throws ScmException
    {
        PerforceClient client = new PerforceClient(configuration, new PerforceWorkspaceManager());
        ScmContextImpl scmContext = createScmContext();
        client.testConnection(scmContext);
        assertEquals("1", client.getLatestRevision(scmContext).toString());
        client.testConnection(scmContext);
    }

    public void testCheckout() throws ScmException
    {
        File workDir = new File(tmpDir, "work");
        assertTrue(workDir.mkdir());
        PerforceClient client = new PerforceClient(configuration, new PerforceWorkspaceManager());
        client.checkout(createExecutionContext(workDir, false), client.getLatestRevision(createScmContext()), new ScmFeedbackAdapter());
        assertTrue("Ant build file should be checked out", new File(workDir, FILE_ANT_BUILD).isFile());
    }

    private ScmContextImpl createScmContext()
    {
        return new ScmContextImpl(null, new PulseExecutionContext());
    }
}