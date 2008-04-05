package com.zutubi.pulse.acceptance;

import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.acceptance.pages.browse.ProjectsPage;
import com.zutubi.pulse.prototype.config.LabelConfiguration;

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
        ProjectsPage projectsPage = new ProjectsPage(selenium, urls);
        projectsPage.goTo();
        projectsPage.assertGroupPresent(g1, p1, p2);
        projectsPage.assertGroupPresent(g2, p1);
        projectsPage.assertGroupPresent(null, p3);
        projectsPage.assertProjectNotPresent(g1, p3);
        projectsPage.assertProjectNotPresent(g2, p2);
        projectsPage.assertProjectNotPresent(g2, p3);
        projectsPage.assertProjectNotPresent(null, p1);
        projectsPage.assertProjectNotPresent(null, p2);
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
        ProjectsPage projectsPage = new ProjectsPage(selenium, urls);
        projectsPage.goTo();
        projectsPage.assertGroupPresent(group, p1);
        projectsPage.assertProjectNotPresent(group, p2);

        insertLabel(p2, label1);

        projectsPage.goTo();
        projectsPage.assertGroupPresent(group, p1, p2);
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
        ProjectsPage projectsPage = new ProjectsPage(selenium, urls);
        projectsPage.goTo();
        projectsPage.assertGroupPresent(group, p1, p2);

        xmlRpcHelper.call("deleteConfig", path);

        projectsPage.goTo();
        projectsPage.assertGroupPresent(group, p1);
        projectsPage.assertProjectNotPresent(group, p2);
    }

    public void testEmptyOutGroup() throws Exception
    {
        String p1 = random + "-1";
        String group = random + "-group";

        xmlRpcHelper.insertSimpleProject(p1,  false);

        Hashtable<String, Object> label1 = createLabel(group);
        String path = insertLabel(p1, label1);

        loginAsAdmin();
        ProjectsPage projectsPage = new ProjectsPage(selenium, urls);
        projectsPage.goTo();
        projectsPage.assertGroupPresent(group, p1);

        xmlRpcHelper.call("deleteConfig", path);

        projectsPage.goTo();
        projectsPage.assertGroupNotPresent(group);
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

}
