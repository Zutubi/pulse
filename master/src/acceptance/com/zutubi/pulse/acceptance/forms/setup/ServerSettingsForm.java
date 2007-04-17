package com.zutubi.pulse.acceptance.forms.setup;

import com.zutubi.pulse.acceptance.forms.BaseForm;
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
        return "com.zutubi.pulse.prototype.config.setup.ServerSettingsConfiguration";
    }

    public String[] getFieldNames()
    {
        return new String[]{"baseUrl", "host", "ssl", "from", "username", "password", "subjectPrefix", "customPort", "port"};
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, TEXTFIELD, CHECKBOX, TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, CHECKBOX, TEXTFIELD };
    }
}
