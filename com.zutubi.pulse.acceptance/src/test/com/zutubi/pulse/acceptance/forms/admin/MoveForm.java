package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;

/**
 * Form used for moving instances to a new template parent.
 */
public class MoveForm extends SeleniumForm
{
    public MoveForm(SeleniumBrowser browser)
    {
        super(browser);
    }

    public String getFormName()
    {
        return "form";
    }

    public String[] getFieldNames()
    {
        return new String[]{"newTemplateParentKey"};
    }

    public int[] getFieldTypes()
    {
        return new int[]{COMBOBOX};
    }

    public void moveFormElements(String... args)
    {
        submitFormElements("confirm", args);
    }
}