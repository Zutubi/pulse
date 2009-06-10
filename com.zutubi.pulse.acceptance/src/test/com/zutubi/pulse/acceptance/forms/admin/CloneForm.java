package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.tove.webwork.CloneAction;

import java.util.LinkedList;
import java.util.List;

/**
 * Form used for cloning map items and template collection items.
 */
public class CloneForm extends SeleniumForm
{
    private boolean smart;
    private List<String> descendents = new LinkedList<String>();

    public CloneForm(SeleniumBrowser browser, boolean smart)
    {
        super(browser);
        this.smart = smart;
    }

    public void addDescendent(String s)
    {
        descendents.add(s);
    }

    public String getFormName()
    {
        return "form";
    }

    public String[] getFieldNames()
    {
        int offset = smart ? 2 : 1;
        String[] fieldNames = new String[descendents.size() * 2 + offset];
        fieldNames[0] = "cloneKey";

        if(smart)
        {
            fieldNames[1] = "parentKey";
        }

        for(int i = 0; i < descendents.size(); i++)
        {
            fieldNames[i * 2 + offset] = CloneAction.CHECK_FIELD_PREFIX + descendents.get(i);
            fieldNames[i * 2 + offset + 1] = CloneAction.KEY_FIELD_PREFIX + descendents.get(i);
        }

        return fieldNames;
    }

    public int[] getFieldTypes()
    {
        int offset = smart ? 2 : 1;
        int fieldTypes[] = new int[descendents.size() * 2 + offset];
        fieldTypes[0] = TEXTFIELD;

        if (smart)
        {
            fieldTypes[1] = TEXTFIELD;
        }

        for(int i = 0; i < descendents.size(); i++)
        {
            fieldTypes[i * 2 + offset] = CHECKBOX;
            fieldTypes[i * 2 + offset + 1] = TEXTFIELD;
        }

        return fieldTypes;
    }

    public void cloneFormElements(String... args)
    {
        submitFormElements("clone", args);
    }
}
