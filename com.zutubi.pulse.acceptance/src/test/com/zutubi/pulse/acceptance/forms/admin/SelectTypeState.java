package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.util.Sort;

import java.util.Collections;
import java.util.List;

/**
 * A form for a type selection state in a wizard.
 */
public class SelectTypeState extends SeleniumForm
{
    private static final String FIELD_NAME_SELECT = "wizard.select";

    public SelectTypeState(SeleniumBrowser browser)
    {
        super(browser);
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

    public List<String> getOptions()
    {
        return getComboBoxOptions(FIELD_NAME_SELECT);
    }

    public List<String> getSortedOptionList()
    {
        List<String> options = getComboBoxOptions(FIELD_NAME_SELECT);
        Collections.sort(options, new Sort.StringComparator());
        return options;
    }
}
