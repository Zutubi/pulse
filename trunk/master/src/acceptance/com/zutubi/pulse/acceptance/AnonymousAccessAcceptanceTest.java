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
        ensureLoggedOut();

        LoginForm form = new LoginForm(tester);
        form.assertFormPresent();
    }

    public void testEnableDisableAccess()
    {
        // @Requires("project")
        // TODO: ensure that a project has been created before this test is executed.

        loginAsAdmin();
        clickLink(Navigation.TAB_ADMINISTRATION);
        clickLink(Navigation.Administration.LINK_EDIT_GENERAL);

        GeneralConfigurationForm form = new GeneralConfigurationForm(tester);
        form.assertFormPresent();
        form.setCheckboxChecked("anonEnabled", true);
        form.save();

        clickLink(Navigation.LINK_LOGOUT);
        assertLinkPresent(Navigation.TAB_PROJECTS);
        assertLinkPresent("login");

        loginAsAdmin();
        clickLink(Navigation.TAB_ADMINISTRATION);
        clickLink(Navigation.Administration.LINK_EDIT_GENERAL);

        form.assertFormPresent();
        form.setCheckboxChecked("anonEnabled", false);
        form.save();

        clickLink(Navigation.LINK_LOGOUT);

        LoginForm login = new LoginForm(tester);
        login.assertFormPresent();
    }
}
