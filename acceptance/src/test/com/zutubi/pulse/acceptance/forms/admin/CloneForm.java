package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.prototype.webwork.CloneAction;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;

/**
 * Form used for cloning map items and template collection items.
 */
public class CloneForm extends SeleniumForm
{
    private String[] descendents;

    public CloneForm(Selenium selenium, String... descendents)
    {
        super(selenium);
        this.descendents = descendents;
    }

    public String getFormName()
    {
        return "form";
    }

    public String[] getFieldNames()
    {
        String[] fieldNames = new String[descendents.length * 2 + 1];
        fieldNames[0] = "cloneKey";
        for(int i = 0; i < descendents.length; i++)
        {
            fieldNames[i * 2 + 1] = CloneAction.CHECK_FIELD_PREFIX + descendents[i];
            fieldNames[i * 2 + 2] = CloneAction.KEY_FIELD_PREFIX + descendents[i];
        }

        return fieldNames;
    }

    public int[] getFieldTypes()
    {
        int fieldTypes[] = new int[descendents.length * 2 + 1];
        fieldTypes[0] = TEXTFIELD;
        for(int i = 1; i < fieldTypes.length; i++)
        {
            if((i % 2) == 0)
            {
                fieldTypes[i] = TEXTFIELD;
            }
            else
            {
                fieldTypes[i] = CHECKBOX;
            }
        }

        return fieldTypes;
    }

    public void cloneFormElements(String... args)
    {
        submitFormElements("clone", args);
    }
}
