package com.zutubi.pulse.acceptance.forms;

import com.thoughtworks.selenium.Selenium;

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
        return new String[]{ "option" };
    }

    public int[] getFieldTypes()
    {
        return new int[]{ COMBOBOX };
    }
}
