/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class CreateContactForm extends BaseForm
{
    public CreateContactForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "contact.create";
    }

    public String[] getFieldNames()
    {
        return new String[]{"contact"};
    }

    public int[] getFieldTypes()
    {
        return new int[]{SELECT};
    }
}
