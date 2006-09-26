package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class PasswordField extends TextField
{
    private static final String TEMPLATE = "password";

    protected String showPassword;

    public String getDefaultTemplate()
    {
        return TEMPLATE;
    }

    public void setShowPassword(String showPassword)
    {
        addParameter("showPassword", showPassword);
    }
}
