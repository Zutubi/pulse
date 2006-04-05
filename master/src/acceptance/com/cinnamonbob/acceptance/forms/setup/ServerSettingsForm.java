package com.cinnamonbob.acceptance.forms.setup;

import com.cinnamonbob.acceptance.forms.BaseForm;
import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class ServerSettingsForm extends BaseForm
{
    public ServerSettingsForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "server.settings";
    }

    public String[] getFieldNames()
    {
        return new String[]{"hostname", "smtpHost", "fromAddress", "username", "password", "prefix"};
    }
}
