/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.forms.BaseForm;
import net.sourceforge.jwebunit.WebTester;

/**
 * The administration edit password form.
 * 
 */
public class EditPasswordForm extends BaseForm
{
    public EditPasswordForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "password.edit";
    }

    public String[] getFieldNames()
    {
        return new String[]{"password", "confirm"};
    }
}
