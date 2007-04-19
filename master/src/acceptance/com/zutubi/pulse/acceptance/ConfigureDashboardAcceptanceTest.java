package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.BaseForm;
import com.zutubi.pulse.acceptance.forms.ConfigureDashboardForm;
import com.zutubi.util.RandomUtils;

/**
 */
public class ConfigureDashboardAcceptanceTest extends BaseAcceptanceTestCase
{
    private static final String P1 = "configdash1";
    private static final String P2 = "configdash2";
    private static final String P3 = "configdash3";
    private static final String G1 = "configdashg1";
    private static final String G2 = "configdashg2";

    private String login;

    protected void setUp() throws Exception
    {
        super.setUp();
        loginAsAdmin();
        ensureProject(P1);
        ensureProject(P2);
        ensureProject(P3);
        ensureProjectGroup(G1, P1, P3);
        ensureProjectGroup(G2, P2, P3);

        navigateToUserAdministration();
        login = "Configure_Dashboard_" + RandomUtils.randomString(5);
        submitCreateUserForm(login, login, login, login);
        logout();
        login(login, login);
        clickLink(Navigation.TAB_DASHBOARD);
    }

    public void testShowAllProjectsDefault()
    {
        assertProjectVisible(P1);
        assertProjectVisible(P2);
        assertProjectVisible(P3);
        assertGroupNotVisible(G1);
        assertGroupNotVisible(G2);
    }

    public void testDontShowAllProjects()
    {
        clickLink(Navigation.LINK_DASHBOARD_CONFIGURE);
        configureProjects(false, "", "");
        assertProjectNotVisible(P1);
        assertProjectNotVisible(P2);
        assertProjectNotVisible(P3);
        assertGroupNotVisible(G1);
        assertGroupNotVisible(G2);
    }

    public void testShowSelectedProjects()
    {
        clickLink(Navigation.LINK_DASHBOARD_CONFIGURE);
        configureProjects(false, P1 + "," + P3, "");
        assertProjectVisible(P1);
        assertProjectNotVisible(P2);
        assertProjectVisible(P3);
        assertGroupNotVisible(G1);
        assertGroupNotVisible(G2);
    }

    public void testShowSelectedGroups()
    {
        clickLink(Navigation.LINK_DASHBOARD_CONFIGURE);
        configureProjects(false, "", G1);
        assertProjectVisible(P1);
        assertProjectNotVisible(P2);
        assertProjectVisible(P3);
        assertGroupVisible(G1);
        assertGroupNotVisible(G2);
    }

    public void testShowAllProjectsAndSelectedGroups()
    {
        clickLink(Navigation.LINK_DASHBOARD_CONFIGURE);
        configureProjects(true, null, G2);
        assertProjectVisible(P1);
        assertProjectVisible(P2);
        assertProjectVisible(P3);
        assertGroupNotVisible(G1);
        assertGroupVisible(G2);
    }

    private ConfigureDashboardForm configureProjects(boolean showAll, String projects, String groups)
    {
        ConfigureDashboardForm form = new ConfigureDashboardForm(tester);
        form.assertFormPresent();
        projects = convertValues(form, "projects", projects);
        groups = convertValues(form, "shownGroups", groups);
        form.saveFormElements(null, Boolean.toString(showAll), projects, groups, null, null, null, null);
        return form;
    }

    private String convertValues(BaseForm form, String field, String names)
    {
        if(names == null)
        {
            return null;
        }

        if(names.length() == 0)
        {
            return names;
        }
        
        String[] nameArray = names.split(",");
        String result = "";

        for(String name: nameArray)
        {
            if(result.length() > 0)
            {
                result += ",";
            }

            result += form.getOptionValue(field, name);
        }

        return result;
    }

    private void assertGroupVisible(String name)
    {
        assertTablePresent("group_" + name);
    }

    private void assertGroupNotVisible(String name)
    {
        assertTableNotPresent("group_" + name);
    }

    private void assertProjectVisible(String name)
    {
        assertLinkPresent("home_" + name);
    }

    private void assertProjectNotVisible(String name)
    {
        assertLinkNotPresent("home_" + name);
    }
}
