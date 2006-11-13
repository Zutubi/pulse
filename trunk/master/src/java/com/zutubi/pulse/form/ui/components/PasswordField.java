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

    protected void evaluateExtraParameters()
    {
        super.evaluateExtraParameters();

        if (showPassword != null)
        {
            addParameter("showPassword", showPassword);
        }
    }

    public void setShowPassword(String showPassword)
    {
        this.showPassword = showPassword;
    }
}
