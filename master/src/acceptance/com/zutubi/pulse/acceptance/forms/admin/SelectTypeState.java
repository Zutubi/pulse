package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;

/**
 * A form for a type selection state in a wizard.
 */
public class SelectTypeState extends SeleniumForm
{
    public SelectTypeState(Selenium selenium)
    {
        super(selenium);
    }

    public String getFormName()
    {
        return "select.state";
    }

    public String[] getFieldNames()
    {
        return new String[]{ "wizard.select" };
    }

    public int[] getFieldTypes()
    {
        return new int[]{ COMBOBOX };
    }
}
