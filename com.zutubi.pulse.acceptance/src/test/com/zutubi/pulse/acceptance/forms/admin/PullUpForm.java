package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;

/**
 * Form used for pulling up composites.
 */
public class PullUpForm extends SeleniumForm
{
    public PullUpForm(SeleniumBrowser browser)
    {
        super(browser);
    }

    public String getFormName()
    {
        return "form";
    }

    public String[] getFieldNames()
    {
        return new String[]{"ancestorKey"};
    }

    public int[] getFieldTypes()
    {
        return new int[]{COMBOBOX};
    }

    public void pullUpFormElements(String... args)
    {
        submitFormElements("pull up", args);
    }
}
