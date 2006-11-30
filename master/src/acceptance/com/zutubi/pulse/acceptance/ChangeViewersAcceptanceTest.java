package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.*;

/**
 */
public class ChangeViewersAcceptanceTest extends ProjectAcceptanceTestBase
{
    private static final String CHANGELIST_VARIABLES = "${revision} ${author} ${branch} ${time.fisheye} ${time.pulse}";
    private static final String FILE_VARIABLES = "${path} ${revision} ${previous.revision}";

    public ChangeViewersAcceptanceTest()
    {
        super(Type.ANT);
    }

    public ChangeViewersAcceptanceTest(String name)
    {
        super(name, Type.ANT);
    }

    public void testNoChangeViewer()
    {
        assertTableRowEqual("project.change.viewer", 1, new String[]{ "no change viewer configured" });
    }

    public void testAddCustomChangeViewer()
    {
        clickLink(Navigation.Projects.LINK_EDIT_CHANGE_VIEWER);
        ConfigureChangeViewerForm form = new ConfigureChangeViewerForm(tester);
        form.assertFormPresent();
        form.nextFormElements("custom");

        ConfigureCustomChangeViewerForm custom = new ConfigureCustomChangeViewerForm(tester);
        custom.assertFormPresent();
        custom.finishFormElements(CHANGELIST_VARIABLES, FILE_VARIABLES, FILE_VARIABLES, FILE_VARIABLES);
        assertChangeViewerTable("details", "custom");

        clickLink(Navigation.Projects.LINK_EDIT_CHANGE_VIEWER);
        custom.assertFormPresent();
        custom.assertFormElements(CHANGELIST_VARIABLES, FILE_VARIABLES, FILE_VARIABLES, FILE_VARIABLES);
    }

    public void testAddFisheyeChangeViewer()
    {
        clickLink(Navigation.Projects.LINK_EDIT_CHANGE_VIEWER);
        ConfigureChangeViewerForm form = new ConfigureChangeViewerForm(tester);
        form.assertFormPresent();
        form.nextFormElements("Fisheye");

        ConfigureFisheyeChangeViewerForm fisheye = new ConfigureFisheyeChangeViewerForm(tester);
        fisheye.assertFormPresent();
        fisheye.finishFormElements("baseurl", "projectpath");
        assertChangeViewerTable("details", "Fisheye [baseurl]");

        clickLink(Navigation.Projects.LINK_EDIT_CHANGE_VIEWER);
        fisheye.assertFormPresent();
        fisheye.assertFormElements("baseurl", "projectpath");
    }

    public void testAddFisheyeChangeViewerValidation()
    {
        clickLink(Navigation.Projects.LINK_EDIT_CHANGE_VIEWER);
        ConfigureChangeViewerForm form = new ConfigureChangeViewerForm(tester);
        form.assertFormPresent();
        form.nextFormElements("Fisheye");

        ConfigureFisheyeChangeViewerForm fisheye = new ConfigureFisheyeChangeViewerForm(tester);
        fisheye.assertFormPresent();
        fisheye.finishFormElements("", "");
        fisheye.assertFormPresent();
        assertTextPresent("base fisheye url is required");
    }

    public void testAddTracChangeViewer()
    {
        clickLink(Navigation.Projects.LINK_EDIT_CHANGE_VIEWER);
        ConfigureChangeViewerForm form = new ConfigureChangeViewerForm(tester);
        form.assertFormPresent();
        form.nextFormElements("Trac");

        ConfigureTracChangeViewerForm trac = new ConfigureTracChangeViewerForm(tester);
        trac.assertFormPresent();
        trac.finishFormElements("baseurl");
        assertChangeViewerTable("details", "Trac [baseurl]");

        clickLink(Navigation.Projects.LINK_EDIT_CHANGE_VIEWER);
        trac.assertFormPresent();
        trac.assertFormElements("baseurl");
    }

    public void testAddTracChangeViewerValidation()
    {
        clickLink(Navigation.Projects.LINK_EDIT_CHANGE_VIEWER);
        ConfigureChangeViewerForm form = new ConfigureChangeViewerForm(tester);
        form.assertFormPresent();
        form.nextFormElements("Trac");

        ConfigureTracChangeViewerForm trac = new ConfigureTracChangeViewerForm(tester);
        trac.assertFormPresent();
        trac.finishFormElements("");
        trac.assertFormPresent();
        assertTextPresent("base trac url is required");
    }

    public void testAddP4WebChangeViewer()
    {
        clickLink(Navigation.Projects.LINK_EDIT_CHANGE_VIEWER);
        ConfigureChangeViewerForm form = new ConfigureChangeViewerForm(tester);
        form.assertFormPresent();
        form.nextFormElements("P4Web");

        ConfigureP4WebChangeViewerForm p4 = new ConfigureP4WebChangeViewerForm(tester);
        p4.assertFormPresent();
        p4.finishFormElements("baseurl");
        assertChangeViewerTable("details", "P4Web [baseurl]");

        clickLink(Navigation.Projects.LINK_EDIT_CHANGE_VIEWER);
        p4.assertFormPresent();
        p4.assertFormElements("baseurl");
    }

    public void testAddP4WebChangeViewerValidation()
    {
        clickLink(Navigation.Projects.LINK_EDIT_CHANGE_VIEWER);
        ConfigureChangeViewerForm form = new ConfigureChangeViewerForm(tester);
        form.assertFormPresent();
        form.nextFormElements("P4Web");

        ConfigureP4WebChangeViewerForm p4 = new ConfigureP4WebChangeViewerForm(tester);
        p4.assertFormPresent();
        p4.finishFormElements("");
        p4.assertFormPresent();
        assertTextPresent("base p4web url is required");
    }

    public void testAddViewVCChangeViewer()
    {
        clickLink(Navigation.Projects.LINK_EDIT_CHANGE_VIEWER);
        ConfigureChangeViewerForm form = new ConfigureChangeViewerForm(tester);
        form.assertFormPresent();
        form.nextFormElements("ViewVC");

        ConfigureViewVCChangeViewerForm viewVC = new ConfigureViewVCChangeViewerForm(tester);
        viewVC.assertFormPresent();
        viewVC.finishFormElements("baseurl", "projectpath");
        assertChangeViewerTable("details", "ViewVC [baseurl]");

        clickLink(Navigation.Projects.LINK_EDIT_CHANGE_VIEWER);
        viewVC.assertFormPresent();
        viewVC.assertFormElements("baseurl", "projectpath");
    }

    public void testAddViewVCChangeViewerValidation()
    {
        clickLink(Navigation.Projects.LINK_EDIT_CHANGE_VIEWER);
        ConfigureChangeViewerForm form = new ConfigureChangeViewerForm(tester);
        form.assertFormPresent();
        form.nextFormElements("ViewVC");

        ConfigureViewVCChangeViewerForm viewVC = new ConfigureViewVCChangeViewerForm(tester);
        viewVC.assertFormPresent();
        viewVC.finishFormElements("", "");
        viewVC.assertFormPresent();
        assertTextPresent("base viewvc url is required");
    }

    private void assertChangeViewerTable(String type, String details)
    {
        assertTableRowEqual("project.change.viewer", 1, new String[]{ type, details });
    }
}
