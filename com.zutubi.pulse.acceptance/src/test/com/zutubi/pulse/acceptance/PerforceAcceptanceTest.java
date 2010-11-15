package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.support.PerforceUtils;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.p4.PerforceCore;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.util.Condition;

import java.util.Hashtable;

import static com.zutubi.pulse.core.test.TestUtils.waitForCondition;

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

        xmlRpcHelper.loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testSimpleBuild() throws Exception
    {
        simpleBuildTest(PerforceUtils.createSpecConfig(xmlRpcHelper));
    }

    public void testManualView() throws Exception
    {
        simpleBuildTest(PerforceUtils.createViewConfig(xmlRpcHelper, PerforceUtils.TRIVIAL_VIEW));
    }

    private void simpleBuildTest(Hashtable<String, Object> p4Config) throws Exception
    {
        String project = randomName();
        xmlRpcHelper.insertSingleCommandProject(project, ProjectManager.GLOBAL_PROJECT_NAME, false, p4Config, xmlRpcHelper.getAntConfig());
        int buildId = xmlRpcHelper.runBuild(project);
        assertEquals(ResultState.SUCCESS, xmlRpcHelper.getBuildStatus(project, buildId));
    }

    public void testClientsCleanedUp() throws Exception
    {
        assertTrue(PerforceUtils.getAllPulseWorkspaces(core).isEmpty());

        String project = randomName();
        String projectPath = xmlRpcHelper.insertSingleCommandProject(project, ProjectManager.GLOBAL_PROJECT_NAME, false, PerforceUtils.createSpecConfig(xmlRpcHelper), xmlRpcHelper.getAntConfig());
        xmlRpcHelper.runBuild(project);

        assertFalse(PerforceUtils.getAllPulseWorkspaces(core).isEmpty());

        xmlRpcHelper.deleteConfig(projectPath);

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