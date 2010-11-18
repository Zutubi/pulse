package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.support.PerforceUtils;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.p4.PerforceCore;
import static com.zutubi.pulse.core.test.TestUtils.waitForCondition;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.util.Condition;

import java.util.Hashtable;

/**
 * Runs simple projects that use a Perforce depot.
 */
public class PerforceAcceptanceTest extends AcceptanceTestBase
{
    private static final long WORKSPACE_TIMEOUT = 30000;

    private PerforceCore core;

    protected void setUp() throws Exception
    {
        super.setUp();

        core = PerforceUtils.createCore();
        PerforceUtils.deleteAllPulseWorkspaces(core);

        rpcClient.loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        rpcClient.logout();
        super.tearDown();
    }

    public void testSimpleBuild() throws Exception
    {
        simpleBuildTest(PerforceUtils.createSpecConfig(rpcClient.RemoteApi));
    }

    public void testManualView() throws Exception
    {
        simpleBuildTest(PerforceUtils.createViewConfig(rpcClient.RemoteApi, PerforceUtils.TRIVIAL_VIEW));
    }

    private void simpleBuildTest(Hashtable<String, Object> p4Config) throws Exception
    {
        String project = randomName();
        rpcClient.RemoteApi.insertSingleCommandProject(project, ProjectManager.GLOBAL_PROJECT_NAME, false, p4Config, rpcClient.RemoteApi.getAntConfig());
        int buildId = rpcClient.RemoteApi.runBuild(project);
        assertEquals(ResultState.SUCCESS, rpcClient.RemoteApi.getBuildStatus(project, buildId));
    }

    public void testClientsCleanedUp() throws Exception
    {
        assertTrue(PerforceUtils.getAllPulseWorkspaces(core).isEmpty());

        String project = randomName();
        String projectPath = rpcClient.RemoteApi.insertSingleCommandProject(project, ProjectManager.GLOBAL_PROJECT_NAME, false, PerforceUtils.createSpecConfig(rpcClient.RemoteApi), rpcClient.RemoteApi.getAntConfig());
        rpcClient.RemoteApi.runBuild(project);

        assertFalse(PerforceUtils.getAllPulseWorkspaces(core).isEmpty());

        rpcClient.RemoteApi.deleteConfig(projectPath);

        waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                try
                {
                    return PerforceUtils.getAllPulseWorkspaces(core).isEmpty();
                }
                catch (ScmException e)
                {
                    return false;
                }
            }
        }, WORKSPACE_TIMEOUT, "Workspaces to be cleaned up");
    }
}