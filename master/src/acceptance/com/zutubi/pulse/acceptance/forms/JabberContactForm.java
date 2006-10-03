package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class JabberContactForm extends BaseForm
{
    private boolean create;

    public JabberContactForm(WebTester tester, boolean create)
    {
        super(tester);
        this.create = create;
    }

    public String getFormName()
    {
        if(create)
        {
            return "JabberNotificationHandler";
        }
        else
        {
            return "jabber.edit";
        }
    }

    public String[] getFieldNames()
    {
        if (create)
        {
            return new String[]{"name", "username"};
        }
        else
        {
            return new String[]{"contact.name", "contact.username"};
        }
    }

    public int[] getFieldTypes()
    {
        return new int[]{TEXTFIELD, TEXTFIELD};
    }

    public void saveFormElements(String... args)
    {
        if(create)
        {
            super.nextFormElements(args);
        }
        else
        {
            super.saveFormElements(args);
        }
    }
}
