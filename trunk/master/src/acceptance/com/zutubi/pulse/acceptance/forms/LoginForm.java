package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class LoginForm extends BaseForm
{
    public LoginForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "j_acegi_security_check";
    }

    public String[] getFieldNames()
    {
        return new String[]{"j_username", "j_password", "_acegi_security_remember_me"};
    }

    public void loginFormElements(String... args)
    {
        setFormElements(args);
        tester.submit("login");
    }

    public int[] getFieldTypes()
    {
        return new int[]{TEXTFIELD, TEXTFIELD, CHECKBOX};
    }
}
