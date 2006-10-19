package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class EmailContactForm extends BaseForm
{

    private boolean create;

    public EmailContactForm(WebTester tester, boolean create)
    {
        super(tester);
        this.create = create;
    }

    public String getFormName()
    {
        if (create)
        {
            return "EmailNotificationHandler";
        }
        else
        {
            return "email.edit";
        }
    }

    public String[] getFieldNames()
    {
        if (create)
        {
            return new String[]{ "name", "email" };
        }
        else
        {
            return new String[]{"contact.name", "contact.email" };
        }
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
    
    public int[] getFieldTypes()
    {
        return new int[]{TEXTFIELD, TEXTFIELD};
    }
}
