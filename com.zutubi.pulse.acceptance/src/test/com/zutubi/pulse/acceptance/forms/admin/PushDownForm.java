package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;

/**
 * Form used for pushing down composites.
 */
public class PushDownForm extends SeleniumForm
{
    public PushDownForm(SeleniumBrowser browser)
    {
        super(browser);
    }

    public String getFormName()
    {
        return "form";
    }

    public String[] getFieldNames()
    {
        return new String[]{"childKeys"};
    }

    public int[] getFieldTypes()
    {
        return new int[]{ITEM_PICKER};
    }

    public void pushDownFormElements(String... args)
    {
        submitFormElements("push down", args);
    }
}