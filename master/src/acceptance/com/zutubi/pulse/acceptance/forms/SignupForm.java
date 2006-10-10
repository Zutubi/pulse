package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class comment/>
 */
public class SignupForm extends BaseForm
{
    public SignupForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "signup";
    }

    public String[] getFieldNames()
    {
        return new String[]{"newUser.login", "newUser.name", "newUser.password", "confirm"};
    }
}
