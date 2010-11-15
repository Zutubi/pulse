package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.ProjectLogPage;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.Condition;

import java.util.Hashtable;

import static com.zutubi.pulse.core.test.TestUtils.waitForCondition;

/**
 * Tests the initialisation/destroy cycle for projects.
 */
public class ProjectLifecycleAcceptanceTest extends AcceptanceTestBase
{
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        xmlRpcHelper.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testManualReinitialise() throws Exception
    {
        String path = xmlRpcHelper.insertSimpleProject(random, false);
        assertProjectState(random, Project.State.IDLE);

        xmlRpcHelper.doConfigAction(path, ProjectConfigurationActions.ACTION_INITIALISE);

        assertProjectHasReinitialised(random);
    }

    public void testReinitialiseOnScmEdit() throws Exception
    {
        String path = xmlRpcHelper.insertSimpleProject(random, false);
        assertProjectState(random, Project.State.IDLE);

        String scmPath = PathUtils.getPath(path, Constants.Project.SCM);
        Hashtable<String, Object> scmConfig = xmlRpcHelper.getConfig(scmPath);
        scmConfig.put(Constants.Project.Scm.QUIET_PERIOD_ENABLED, true);
        xmlRpcHelper.saveConfig(scmPath, scmConfig, false);

        assertProjectHasReinitialised(random);
    }

    public void testDeleteAndReAddScm() throws Exception
    {
        String path = xmlRpcHelper.insertSimpleProject(random, false);
        assertProjectState(random, Project.State.IDLE);

        String scmPath = PathUtils.getPath(path, Constants.Project.SCM);
        Hashtable<String, Object> scmConfig = xmlRpcHelper.getConfig(scmPath);
        xmlRpcHelper.deleteConfig(scmPath);

        waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                try
                {
                    return xmlRpcHelper.getProjectState(random) == Project.State.INITIAL;
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        }, 30000, "project '" + random + "' to return to the initial state");

        xmlRpcHelper.insertConfig(scmPath, scmConfig);
        xmlRpcHelper.waitForProjectToInitialise(random);
    }

    private void assertProjectState(String project, Project.State expectedState) throws Exception
    {
        assertEquals(expectedState, xmlRpcHelper.getProjectState(project));
    }

    private void assertProjectHasReinitialised(String project) throws Exception
    {
        xmlRpcHelper.waitForProjectToInitialise(random);

        getBrowser().loginAsAdmin();
        getBrowser().openAndWaitFor(ProjectLogPage.class, project);
        assertTrue(getBrowser().isTextPresent("Reinitialising"));
    }
}
