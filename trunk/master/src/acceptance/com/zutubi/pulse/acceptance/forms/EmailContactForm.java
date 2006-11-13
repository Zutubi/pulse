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
        return "EmailNotificationHandler";
    }

    public String[] getFieldNames()
    {
        return new String[]{ "name", "email" };
    }

    public void saveFormElements(String... args)
    {
        if(create)
        {
            super.finishFormElements(args);
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
