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

import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.acceptance.utils.BuildRunner;
import com.zutubi.pulse.acceptance.utils.UserConfigurations;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.Condition;
import com.zutubi.util.io.FileSystemUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.File;
import java.util.Hashtable;

/**
 * Tests for pinning and unpinning builds.
 */
public class BuildPinningAcceptanceTest extends AcceptanceTestBase
{
    private static final String TEST_PROJECT = "pinning-test-project";
    private static final String TEST_USER = "pinning-user";
    private static final int PIN_TIMEOUT = 30000;

    private BuildRunner buildRunner;
    private File tempDir;
    private UserConfigurations users;
    private ProjectConfiguration testProject;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        tempDir = FileSystemUtils.createTempDir(getName());

        rpcClient.loginAsAdmin();

        buildRunner = new BuildRunner(rpcClient.RemoteApi);

        if (!CONFIGURATION_HELPER.isProjectExists(TEST_PROJECT))
        {
            CONFIGURATION_HELPER.insertProject(projectConfigurations.createTrivialAntProject(TEST_PROJECT).getConfig(), false);
        }

        users = new UserConfigurations();
        if (!CONFIGURATION_HELPER.isUserExists(TEST_USER))
        {
            CONFIGURATION_HELPER.insertUser(users.createSimpleUser(TEST_USER));
        }

        testProject = CONFIGURATION_HELPER.getConfigurationReference("projects/" + TEST_PROJECT, ProjectConfiguration.class);
    }

    @Override
    protected void tearDown() throws Exception
    {
        rpcClient.cancelIncompleteBuilds();
        rpcClient.logout();

        removeDirectory(tempDir);

        super.tearDown();
    }

    public void testPinAndUnpinViaApi() throws Exception
    {
        int buildId = buildRunner.triggerAndWaitForBuild(testProject);

        // Unpinned by default, can't unpin
        assertFalse(isBuildPinned(buildId));
        assertFalse(rpcClient.RemoteApi.unpinBuild(TEST_PROJECT, buildId));

        // Pin, can't delete.
        assertTrue(rpcClient.RemoteApi.pinBuild(TEST_PROJECT, buildId));
        assertTrue(isBuildPinned(buildId));
        assertFalse(rpcClient.RemoteApi.deleteBuild(TEST_PROJECT, buildId));
        assertTrue(isBuildPinned(buildId));

        // Can't pin when already pinned.
        assertFalse(rpcClient.RemoteApi.pinBuild(TEST_PROJECT, buildId));
        assertTrue(isBuildPinned(buildId));

        // Unpin and delete
        assertTrue(rpcClient.RemoteApi.unpinBuild(TEST_PROJECT, buildId));
        assertFalse(isBuildPinned(buildId));
        assertTrue(rpcClient.RemoteApi.deleteBuild(TEST_PROJECT, buildId));
    }

    public void testPinAndUnpinViaUI() throws Exception
    {
        int buildId = buildRunner.triggerAndWaitForBuild(testProject);

        getBrowser().loginAsAdmin();
        final BuildSummaryPage page = getBrowser().openAndWaitFor(BuildSummaryPage.class, TEST_PROJECT, (long) buildId);
        assertFalse(page.getBuildTitle().contains("pinned"));
        assertTrue(page.isActionPresent(BuildResult.ACTION_PIN));
        assertFalse(page.isActionPresent(BuildResult.ACTION_UNPIN));
        assertTrue(page.isActionPresent(AccessManager.ACTION_DELETE));

        page.clickAction(BuildResult.ACTION_PIN);
        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return page.getBuildTitle().contains("pinned");
            }
        }, PIN_TIMEOUT, "build to be pinned");
        page.waitFor();
        assertFalse(page.isActionPresent(BuildResult.ACTION_PIN));
        assertTrue(page.isActionPresent(BuildResult.ACTION_UNPIN));
        assertFalse(page.isActionPresent(AccessManager.ACTION_DELETE));
        assertTrue(isBuildPinned(buildId));

        page.clickAction(BuildResult.ACTION_UNPIN);
        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return !page.getBuildTitle().contains("pinned");
            }
        }, PIN_TIMEOUT, "build to be unpinned");
        page.waitFor();
        assertTrue(page.isActionPresent(BuildResult.ACTION_PIN));
        assertFalse(page.isActionPresent(BuildResult.ACTION_UNPIN));
        assertTrue(page.isActionPresent(AccessManager.ACTION_DELETE));
        assertFalse(isBuildPinned(buildId));
    }

    public void testPinPermission() throws Exception
    {
        int buildId = buildRunner.triggerAndWaitForBuild(testProject);

        getBrowser().login(TEST_USER, "");
        final BuildSummaryPage page = getBrowser().openAndWaitFor(BuildSummaryPage.class, TEST_PROJECT, (long) buildId);
        assertFalse(page.isActionPresent(BuildResult.ACTION_PIN));
        assertFalse(page.isActionPresent(BuildResult.ACTION_UNPIN));

        assertTrue(rpcClient.RemoteApi.pinBuild(TEST_PROJECT, buildId));
        page.openAndWaitFor();
        assertFalse(page.isActionPresent(BuildResult.ACTION_PIN));
        assertFalse(page.isActionPresent(BuildResult.ACTION_UNPIN));

        rpcClient.logout();
        try
        {
            rpcClient.login(TEST_USER, "");
            rpcClient.RemoteApi.unpinBuild(TEST_PROJECT, buildId);
            fail("Random user can't unpin builds");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("AccessDeniedException"));
        }
        finally
        {
            rpcClient.logout();
            rpcClient.loginAsAdmin();
        }
    }

    private boolean isBuildPinned(int buildId) throws Exception
    {
        Hashtable<String, Object> build = rpcClient.RemoteApi.getBuild(TEST_PROJECT, buildId);
        return (Boolean) build.get("pinned");
    }
}
