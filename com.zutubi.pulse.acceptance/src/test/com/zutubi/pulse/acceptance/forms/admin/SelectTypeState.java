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
    private static final String FIELD_NAME_SELECT = "wizard.select";

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
        return new String[]{FIELD_NAME_SELECT};
    }

    public int[] getFieldTypes()
    {
        return new int[]{ COMBOBOX };
    }

    public String[] getOptions()
    {
        return getComboBoxOptions(FIELD_NAME_SELECT);
    }

    public List<String> getSortedOptionList()
    {
        String[] options = getComboBoxOptions(FIELD_NAME_SELECT);
        Arrays.sort(options, new Sort.StringComparator());
        return Arrays.asList(options);
    }
}
