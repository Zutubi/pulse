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

package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectLogPage;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.Condition;
import com.zutubi.util.EnumUtils;

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
        rpcClient.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        rpcClient.logout();
        super.tearDown();
    }

    public void testManualReinitialise() throws Exception
    {
        String path = rpcClient.RemoteApi.insertSimpleProject(random, false);
        assertProjectState(random, Project.State.IDLE);

        rpcClient.RemoteApi.doConfigAction(path, ProjectConfigurationActions.ACTION_INITIALISE);

        assertProjectHasReinitialised(random);
    }

    public void testReinitialiseOnScmEdit() throws Exception
    {
        String path = rpcClient.RemoteApi.insertSimpleProject(random, false);
        assertProjectState(random, Project.State.IDLE);

        String scmPath = PathUtils.getPath(path, Constants.Project.SCM);
        Hashtable<String, Object> scmConfig = rpcClient.RemoteApi.getConfig(scmPath);
        scmConfig.put("useExport", true);
        rpcClient.RemoteApi.saveConfig(scmPath, scmConfig, false);

        assertProjectHasReinitialised(random);
    }

    public void testDeleteAndReAddScm() throws Exception
    {
        String path = rpcClient.RemoteApi.insertSimpleProject(random, false);
        assertProjectState(random, Project.State.IDLE);

        String scmPath = PathUtils.getPath(path, Constants.Project.SCM);
        Hashtable<String, Object> scmConfig = rpcClient.RemoteApi.getConfig(scmPath);
        rpcClient.RemoteApi.deleteConfig(scmPath);

        waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                try
                {
                    return rpcClient.RemoteApi.getProjectState(random) == Project.State.INITIAL;
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        }, 30000, "project '" + random + "' to return to the initial state");

        rpcClient.RemoteApi.insertConfig(scmPath, scmConfig);
        rpcClient.RemoteApi.waitForProjectToInitialise(random);
    }

    public void testPauseResume() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleProject(random);
        assertProjectState(random, Project.State.IDLE);

        getBrowser().loginAsAdmin();
        final ProjectHomePage homePage = getBrowser().openAndWaitFor(ProjectHomePage.class, random);
        assertTrue(homePage.isTransitionLinkPresent(Project.Transition.PAUSE));
        assertFalse(homePage.isTransitionLinkPresent(Project.Transition.RESUME));

        homePage.clickTransitionLink(Project.Transition.PAUSE);
        TestUtils.waitForCondition(new ProjectStateCondition(homePage, Project.State.PAUSED), SeleniumBrowser.DEFAULT_TIMEOUT, "project to become paused");

        assertFalse(homePage.isTransitionLinkPresent(Project.Transition.PAUSE));
        assertTrue(homePage.isTransitionLinkPresent(Project.Transition.RESUME));

        homePage.clickTransitionLink(Project.Transition.RESUME);
        TestUtils.waitForCondition(new ProjectStateCondition(homePage, Project.State.IDLE), SeleniumBrowser.DEFAULT_TIMEOUT, "project to become idle");

        assertTrue(homePage.isTransitionLinkPresent(Project.Transition.PAUSE));
        assertFalse(homePage.isTransitionLinkPresent(Project.Transition.RESUME));
    }

    public void testInitialisationFailed() throws Exception
    {
        rpcClient.RemoteApi.insertSingleCommandProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false, rpcClient.RemoteApi.getGitConfig("bad url"), rpcClient.RemoteApi.getAntConfig());
        assertProjectState(random, Project.State.INITIALISATION_FAILED);

        getBrowser().loginAsAdmin();
        ProjectHomePage homePage = getBrowser().openAndWaitFor(ProjectHomePage.class, random);
        assertEquals(EnumUtils.toPrettyString(Project.State.INITIALISATION_FAILED), homePage.getState());
        assertTrue(homePage.isTransitionLinkPresent(Project.Transition.INITIALISE));
    }

    private void assertProjectState(String project, Project.State expectedState) throws Exception
    {
        assertEquals(expectedState, rpcClient.RemoteApi.getProjectState(project));
    }

    private void assertProjectHasReinitialised(String project) throws Exception
    {
        rpcClient.RemoteApi.waitForProjectToInitialise(random);

        getBrowser().loginAsAdmin();
        getBrowser().openAndWaitFor(ProjectLogPage.class, project);
        getBrowser().waitForTextPresent("Reinitialising");
    }

    private static class ProjectStateCondition implements Condition
    {
        private final ProjectHomePage homePage;
        private Project.State state;

        public ProjectStateCondition(ProjectHomePage homePage, Project.State state)
        {
            this.homePage = homePage;
            this.state = state;
        }

        public boolean satisfied()
        {
            return homePage.getState().equals(EnumUtils.toPrettyString(state));
        }
    }
}
