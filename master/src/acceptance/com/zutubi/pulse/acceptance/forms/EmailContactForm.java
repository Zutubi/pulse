/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class EmailContactForm extends BaseForm
{

    public EmailContactForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "email.edit";
    }

    public String[] getFieldNames()
    {
        return new String[]{"contact.name", "contact.email", "contact.type"};
    }

    public int[] getFieldTypes()
    {
        return new int[]{TEXTFIELD, TEXTFIELD, RADIOBOX};
    }
}
