package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.BrowsePage;
import com.zutubi.tove.type.record.PathUtils;

import java.util.Hashtable;

/**
 * Acceptance tests that verify categorisation of projects using labels.
 */
public class ProjectLabelAcceptanceTest extends AcceptanceTestBase
{
    protected void setUp() throws Exception
    {
        super.setUp();
        rpcClient.loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        rpcClient.logout();
        super.tearDown();
    }

    public void testSimpleGroups() throws Exception
    {
        String p1 = random + "-p1";
        String p2 = random + "-p2";
        String p3 = random + "-p3";
        String g1 = random + "-g1";
        String g2 = random + "-g2";

        rpcClient.RemoteApi.insertSimpleProject(p1,  false);
        rpcClient.RemoteApi.insertSimpleProject(p2,  false);
        rpcClient.RemoteApi.insertSimpleProject(p3,  false);

        rpcClient.RemoteApi.addLabel(p1, g1);
        rpcClient.RemoteApi.addLabel(p1, g2);
        rpcClient.RemoteApi.addLabel(p2, g1);

        getBrowser().loginAsAdmin();
        BrowsePage browsePage = getBrowser().openAndWaitFor(BrowsePage.class);
        assertGroupPresent(browsePage, g1, p1, p2);
        assertGroupPresent(browsePage, g2, p1);
        assertGroupPresent(browsePage, null, p3);
        assertFalse(browsePage.isProjectPresent(g1, p3));
        assertFalse(browsePage.isProjectPresent(g2, p2));
        assertFalse(browsePage.isProjectPresent(g2, p3));
        assertFalse(browsePage.isProjectPresent(null, p1));
        assertFalse(browsePage.isProjectPresent(null, p2));
    }

    public void testDisableGroupByLabel() throws Exception
    {
        String projectName = random + "-project";
        String labelName = random + "-label";
        String userLogin = random + "-user";

        rpcClient.RemoteApi.insertSimpleProject(projectName,  false);
        rpcClient.RemoteApi.addLabel(projectName, labelName);

        String userPath = rpcClient.RemoteApi.insertTrivialUser(userLogin);
        assertTrue(getBrowser().login(userLogin, ""));

        // Default is group by label
        BrowsePage browsePage = getBrowser().openAndWaitFor(BrowsePage.class);
        assertGroupPresent(browsePage, labelName, projectName);
        assertFalse(browsePage.isProjectPresent(null, projectName));

        // Uncheck option and ensure grouping disappears
        String prefsPath = PathUtils.getPath(userPath, "preferences", "browseView");
        Hashtable<String, Object> browsePreferences = rpcClient.RemoteApi.getConfig(prefsPath);
        browsePreferences.put("groupsShown", false);
        rpcClient.RemoteApi.saveConfig(prefsPath, browsePreferences, false);

        browsePage.openAndWaitFor();
        assertFalse(browsePage.isGroupPresent(labelName));
        assertTrue(browsePage.isProjectPresent(null, projectName));
    }

    public void testAddProjectToGroup() throws Exception
    {
        String p1 = random + "-1";
        String p2 = random + "-2";
        String group = random + "-group";

        rpcClient.RemoteApi.insertSimpleProject(p1,  false);
        rpcClient.RemoteApi.insertSimpleProject(p2,  false);

        rpcClient.RemoteApi.addLabel(p1, group);

        getBrowser().loginAsAdmin();
        BrowsePage browsePage = getBrowser().openAndWaitFor(BrowsePage.class);
        assertGroupPresent(browsePage, group, p1);
        assertFalse(browsePage.isProjectPresent(group, p2));

        rpcClient.RemoteApi.addLabel(p2, group);

        browsePage.openAndWaitFor();
        assertGroupPresent(browsePage, group, p1, p2);
    }

    public void testRemoveProjectFromGroup() throws Exception
    {
        String p1 = random + "-1";
        String p2 = random + "-2";
        String group = random + "-group";

        rpcClient.RemoteApi.insertSimpleProject(p1,  false);
        rpcClient.RemoteApi.insertSimpleProject(p2,  false);

        rpcClient.RemoteApi.addLabel(p1, group);
        String path = rpcClient.RemoteApi.addLabel(p2, group);

        getBrowser().loginAsAdmin();
        BrowsePage browsePage = getBrowser().openAndWaitFor(BrowsePage.class);
        assertGroupPresent(browsePage, group, p1, p2);

        rpcClient.RemoteApi.call("deleteConfig", path);

        browsePage.openAndWaitFor();
        assertGroupPresent(browsePage, group, p1);
        assertFalse(browsePage.isProjectPresent(group, p2));
    }

    public void testEmptyOutGroup() throws Exception
    {
        String p1 = random + "-1";
        String group = random + "-group";

        rpcClient.RemoteApi.insertSimpleProject(p1,  false);

        String path = rpcClient.RemoteApi.addLabel(p1, group);

        getBrowser().loginAsAdmin();
        BrowsePage browsePage = getBrowser().openAndWaitFor(BrowsePage.class);
        assertGroupPresent(browsePage, group, p1);

        rpcClient.RemoteApi.call("deleteConfig", path);

        browsePage.openAndWaitFor();
        assertFalse(browsePage.isGroupPresent(group));
    }

    private void assertGroupPresent(BrowsePage browsePage, String group, String... projects)
    {
        assertTrue("Group '" + group + "' not found", browsePage.isGroupPresent(group));
        for (String project: projects)
        {
            assertTrue("Project '" + project + "' not found in group '" + group + "'", browsePage.isProjectPresent(group, project));
        }
    }
}
