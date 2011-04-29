package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;

/**
 * Form used for introducing new template parents.
 */
public class IntroduceParentForm extends SeleniumForm
{
    public IntroduceParentForm(SeleniumBrowser browser)
    {
        super(browser);
    }

    public String getFormName()
    {
        return "form";
    }

    public String[] getFieldNames()
    {
        return new String[]{"parentKey", "pullUp"};
    }

    public int[] getFieldTypes()
    {
        return new int[]{TEXTFIELD, CHECKBOX};
    }

    public void okFormElements(String... args)
    {
        submitFormElements("ok", args);
    }
}