package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.BrowsePage;
import com.zutubi.pulse.master.tove.config.LabelConfiguration;
import com.zutubi.tove.type.record.PathUtils;

import java.util.Hashtable;

/**
 * Acceptance tests that verify categorisation of projects using labels.
 */
public class ProjectLabelAcceptanceTest extends SeleniumTestBase
{
    protected void setUp() throws Exception
    {
        super.setUp();
        xmlRpcHelper.loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testSimpleGroups() throws Exception
    {
        String p1 = random + "-p1";
        String p2 = random + "-p2";
        String p3 = random + "-p3";
        String g1 = random + "-g1";
        String g2 = random + "-g2";

        xmlRpcHelper.insertSimpleProject(p1,  false);
        xmlRpcHelper.insertSimpleProject(p2,  false);
        xmlRpcHelper.insertSimpleProject(p3,  false);

        Hashtable<String, Object> label1 = createLabel(g1);
        Hashtable<String, Object> label2 = createLabel(g2);
        insertLabel(p1, label1);
        insertLabel(p1, label2);
        insertLabel(p2, label1);

        loginAsAdmin();
        BrowsePage browsePage = browser.openAndWaitFor(BrowsePage.class);
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

        xmlRpcHelper.insertSimpleProject(projectName,  false);
        Hashtable<String, Object> label = createLabel(labelName);
        insertLabel(projectName, label);

        String userPath = xmlRpcHelper.insertTrivialUser(userLogin);
        login(userLogin, "");

        // Default is group by label
        BrowsePage browsePage = browser.openAndWaitFor(BrowsePage.class);
        assertGroupPresent(browsePage, labelName, projectName);
        assertFalse(browsePage.isProjectPresent(null, projectName));

        // Uncheck option and ensure grouping disappears
        String prefsPath = PathUtils.getPath(userPath, "preferences", "browseView");
        Hashtable<String, Object> browsePreferences = xmlRpcHelper.getConfig(prefsPath);
        browsePreferences.put("groupsShown", false);
        xmlRpcHelper.saveConfig(prefsPath, browsePreferences, false);

        browsePage.openAndWaitFor();
        assertFalse(browsePage.isGroupPresent(labelName));
        assertTrue(browsePage.isProjectPresent(null, projectName));
    }

    public void testAddProjectToGroup() throws Exception
    {
        String p1 = random + "-1";
        String p2 = random + "-2";
        String group = random + "-group";

        xmlRpcHelper.insertSimpleProject(p1,  false);
        xmlRpcHelper.insertSimpleProject(p2,  false);

        Hashtable<String, Object> label1 = createLabel(group);
        insertLabel(p1, label1);

        loginAsAdmin();
        BrowsePage browsePage = browser.openAndWaitFor(BrowsePage.class);
        assertGroupPresent(browsePage, group, p1);
        assertFalse(browsePage.isProjectPresent(group, p2));

        insertLabel(p2, label1);

        browsePage.openAndWaitFor();
        assertGroupPresent(browsePage, group, p1, p2);
    }

    public void testRemoveProjectFromGroup() throws Exception
    {
        String p1 = random + "-1";
        String p2 = random + "-2";
        String group = random + "-group";

        xmlRpcHelper.insertSimpleProject(p1,  false);
        xmlRpcHelper.insertSimpleProject(p2,  false);

        Hashtable<String, Object> label1 = createLabel(group);
        insertLabel(p1, label1);
        String path = insertLabel(p2, label1);

        loginAsAdmin();
        BrowsePage browsePage = browser.openAndWaitFor(BrowsePage.class);
        assertGroupPresent(browsePage, group, p1, p2);

        xmlRpcHelper.call("deleteConfig", path);

        browsePage.openAndWaitFor();
        assertGroupPresent(browsePage, group, p1);
        assertFalse(browsePage.isProjectPresent(group, p2));
    }

    public void testEmptyOutGroup() throws Exception
    {
        String p1 = random + "-1";
        String group = random + "-group";

        xmlRpcHelper.insertSimpleProject(p1,  false);

        Hashtable<String, Object> label1 = createLabel(group);
        String path = insertLabel(p1, label1);

        loginAsAdmin();
        BrowsePage browsePage = browser.openAndWaitFor(BrowsePage.class);
        assertGroupPresent(browsePage, group, p1);

        xmlRpcHelper.call("deleteConfig", path);

        browsePage.openAndWaitFor();
        assertFalse(browsePage.isGroupPresent(group));
    }

    private Hashtable<String, Object> createLabel(String name)
    {
        Hashtable<String, Object> label = xmlRpcHelper.createEmptyConfig(LabelConfiguration.class);
        label.put("label", name);
        return label;
    }

    private String insertLabel(String project, Hashtable<String, Object> label) throws Exception
    {
        return xmlRpcHelper.insertConfig(PathUtils.getPath("projects", project, "labels"), label);
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
