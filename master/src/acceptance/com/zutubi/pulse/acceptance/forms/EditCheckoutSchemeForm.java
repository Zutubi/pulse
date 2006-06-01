package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class EditCheckoutSchemeForm extends BaseForm
{
    public EditCheckoutSchemeForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "edit.checkout.scheme";
    }

    public String[] getFieldNames()
    {
        return new String[]{"checkoutScheme"};
    }

    public int[] getFieldTypes()
    {
        return new int[]{SELECT};
    }
}
