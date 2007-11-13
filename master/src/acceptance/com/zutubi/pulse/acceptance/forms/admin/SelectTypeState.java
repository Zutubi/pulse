package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.util.Sort;

import java.util.List;
import java.util.Arrays;

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

    public String[] getOptions()
    {
        return getComboBoxOptions(getFieldNames()[0]);
    }

    public List<String> getSortedOptionList()
    {
        String[] options = getComboBoxOptions(getFieldNames()[0]);
        Arrays.sort(options, new Sort.StringComparator());
        return Arrays.asList(options);
    }
}
