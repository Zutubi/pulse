package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.GeneralConfigurationForm;
import com.zutubi.pulse.acceptance.forms.LoginForm;

/**
 */
public class AnonymousAccessAcceptanceTest extends BaseAcceptanceTestCase
{
    public void testDisabledByDefault()
    {
        beginAt("/");
        if(tester.getDialog().isLinkPresent("logout"))
        {
            clickLink("logout");
        }

        LoginForm form = new LoginForm(tester);
        form.assertFormPresent();
    }

    public void testEnableDisableAccess()
    {
        // @Requires("project")
        // TODO: ensure that a project has been created before this test is executed.

        loginAsAdmin();
        clickLink("tab.administration");
        clickLink("general.edit");

        GeneralConfigurationForm form = new GeneralConfigurationForm(tester);
        form.assertFormPresent();
        form.saveFormElements(null, null, null, "true", "5", null, null);

        clickLink("logout");
        assertLinkPresent("tab.projects");
        assertLinkPresent("login");

        loginAsAdmin();
        clickLink("tab.administration");
        clickLink("general.edit");

        form.assertFormPresent();
        form.saveFormElements(null, null, null, "false", "5", null, null);
        clickLink("logout");

        LoginForm login = new LoginForm(tester);
        login.assertFormPresent();
    }
}
