package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.p4.PerforceConstants;
import com.zutubi.pulse.core.scm.p4.PerforceCore;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Condition;
import com.zutubi.util.Predicate;

import java.util.Hashtable;
import java.util.List;

/**
 * Runs simple projects that use a Perforce depot.
 */
public class PerforceAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private static final String P4PORT = ":6777";
    private static final String P4USER = "pulse";
    private static final String P4PASSWD = "pulse";
    private static final String P4CLIENT = "triviant";

    private static final String WORKSPACE_PREFIX = "pulse-";

    private PerforceCore core;

    protected void setUp() throws Exception
    {
        super.setUp();

        core = createCore();
        for (String workspace: getAllPulseWorkspaces())
        {
            core.deleteWorkspace(workspace);
        }

        xmlRpcHelper.loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testSimpleBuild() throws Exception
    {
        String project = randomName();
        xmlRpcHelper.insertProject(project, ProjectManager.GLOBAL_PROJECT_NAME, false, createPerforceConfig(), xmlRpcHelper.getAntConfig());
        int buildId = xmlRpcHelper.runBuild(project, 30000);
        Hashtable<String, Object> build = xmlRpcHelper.getBuild(project, buildId);
        assertEquals("success", build.get("status"));
    }

    public void testClientsCleanedUp() throws Exception
    {
        assertTrue(getAllPulseWorkspaces().isEmpty());

        String project = randomName();
        String projectPath = xmlRpcHelper.insertProject(project, ProjectManager.GLOBAL_PROJECT_NAME, false, createPerforceConfig(), xmlRpcHelper.getAntConfig());
        xmlRpcHelper.runBuild(project, 30000);

        assertFalse(getAllPulseWorkspaces().isEmpty());

        xmlRpcHelper.deleteConfig(projectPath);

        AcceptanceTestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                try
                {
                    return getAllPulseWorkspaces().isEmpty();
                }
                catch (ScmException e)
                {
                    return false;
                }
            }
        }, 30000, "Workspaces to be cleaned up");
    }

    private List<String> getAllPulseWorkspaces() throws ScmException
    {
        return CollectionUtils.filter(core.getAllWorkspaceNames(), new Predicate<String>()
        {
            public boolean satisfied(String s)
            {
                return s.startsWith(WORKSPACE_PREFIX);
            }
        });
    }

    private PerforceCore createCore()
    {
        PerforceCore core = new PerforceCore();
        core.setEnv(PerforceConstants.ENV_PORT, P4PORT);
        core.setEnv(PerforceConstants.ENV_USER, P4USER);
        core.setEnv(PerforceConstants.ENV_PASSWORD, P4PASSWD);
        core.setEnv(PerforceConstants.ENV_CLIENT, P4CLIENT);
        return core;
    }

    private Hashtable<String, Object> createPerforceConfig() throws Exception
    {
        Hashtable<String, Object> p4Config = xmlRpcHelper.createDefaultConfig("zutubi.perforceConfig");
        p4Config.put("port", P4PORT);
        p4Config.put("user", P4USER);
        p4Config.put("password", P4PASSWD);
        p4Config.put("spec", P4CLIENT);
        return p4Config;
    }
}