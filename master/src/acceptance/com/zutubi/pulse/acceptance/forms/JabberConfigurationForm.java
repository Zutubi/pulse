package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class JabberConfigurationForm extends BaseForm
{
    public JabberConfigurationForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "jabber.config";
    }

    public String[] getFieldNames()
    {
        return new String[]{"jabber.host", "jabber.port", "jabber.username", "jabber.password"};
    }

    public int[] getFieldTypes()
    {
        return new int[]{TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD};
    }
}
