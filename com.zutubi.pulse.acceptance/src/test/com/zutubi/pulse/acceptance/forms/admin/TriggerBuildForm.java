package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.util.Pair;

import java.util.LinkedList;
import java.util.List;

/**
 * The form that is presented to the user when they manually trigger a build and have
 * the build option prompt enabled.
 */
public class TriggerBuildForm extends SeleniumForm
{
    private boolean expectRebuildField = false;

    /**
     * The names of the properties configured for the project.
     */
    private List<String> propertyNames = new LinkedList<String>();

    public TriggerBuildForm(SeleniumBrowser browser)
    {
        super(browser, false);
    }

    public void addProperty(String name)
    {
        propertyNames.add(name);
    }

    public void expectRebuildField()
    {
        expectRebuildField = true;
    }

    public String getFormName()
    {
        return "edit.build.properties";
    }

    public String[] getFieldNames()
    {
        List<String> fieldNames = new LinkedList<String>();
        fieldNames.add("revision");
        fieldNames.add("version");
        fieldNames.add("priority");
        fieldNames.add("status");
        if (expectRebuildField)
        {
            fieldNames.add("rebuild");
        }
        for (String propertyName : propertyNames)
        {
            fieldNames.add("property." + propertyName);
        }

        return fieldNames.toArray(new String[fieldNames.size()]);
    }

    public int[] getFieldTypes()
    {
        List<Integer> types = new LinkedList<Integer>();
        types.add(TEXTFIELD);
        types.add(TEXTFIELD);
        types.add(TEXTFIELD);
        types.add(ITEM_PICKER);
        if (expectRebuildField)
        {
            types.add(CHECKBOX);
        }
        //noinspection UnusedDeclaration
        for (String propertyName : propertyNames)
        {
            types.add(TEXTFIELD);
        }

        int[] result = new int[types.size()];
        for (int i = 0; i < result.length; i++)
        {
            result[i] = types.get(i);
        }
        return result;
    }

    /**
     * The first form argument is the revision, the second is the status, the remaining
     * are the property values, if any.
     * 
     * @param args the argument values
     */
    public void triggerFormElements(Pair<String, String>... args)
    {
        submitNamedFormElements("trigger", args);
    }

    public boolean isRebuildCheckboxPresent()
    {
        return browser.isElementIdPresent(getFieldId("rebuild"));
    }
}
