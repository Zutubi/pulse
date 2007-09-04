package com.zutubi.pulse.acceptance.forms.admin;

/**
 */
public class EmailSettingsCheckForm extends CheckForm
{
    public EmailSettingsCheckForm(EmailSettingsForm mainForm)
    {
        super(mainForm);
    }

    public String[] getFieldNames()
    {
        return new String[]{ "emailAddress"};
    }

    public int[] getFieldTypes()
    {
        return new int[]{ TEXTFIELD };
    }
}
