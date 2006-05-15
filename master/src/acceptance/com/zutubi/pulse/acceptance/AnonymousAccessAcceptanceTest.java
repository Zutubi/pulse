package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.GeneralConfigurationForm;
import com.zutubi.pulse.acceptance.forms.LoginForm;

/**
 */
public class AnonymousAccessAcceptanceTest extends BaseAcceptanceTest
{
    public void testDisabledByDefault()
    {
        gotoPage("/");
        LoginForm form = new LoginForm(tester);
        form.assertFormPresent();
    }

    public void testEnableDisableAccess()
    {
        login("admin", "admin");
        clickLinkWithText("administration");
        clickLink("general.edit");

        GeneralConfigurationForm form = new GeneralConfigurationForm(tester);
        form.assertFormPresent();
        form.saveFormElements(null, null, null, "true");

        clickLink("logout");
        assertTextPresent("projects");
        assertLinkPresent("login");

        login("admin", "admin");
        clickLinkWithText("administration");
        clickLink("general.edit");

        form.assertFormPresent();
        form.saveFormElements(null, null, null, "false");
        clickLink("logout");

        LoginForm login = new LoginForm(tester);
        login.assertFormPresent();
    }
}
