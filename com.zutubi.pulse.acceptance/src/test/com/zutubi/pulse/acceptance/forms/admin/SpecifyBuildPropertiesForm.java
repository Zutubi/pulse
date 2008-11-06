package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.thoughtworks.selenium.Selenium;

import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;

/**
 * The form that is presented to the user when they manually trigger a build and have
 * the build option prompt enabled and have properties configured.
 */
public class SpecifyBuildPropertiesForm extends SeleniumForm
{
    /**
     * The names of the properties configured for the project.
     */
    private List<String> propertyNames = new LinkedList<String>();

    public SpecifyBuildPropertiesForm(Selenium selenium, String... propertyNames)
    {
        super(selenium, false);
        this.propertyNames = Arrays.asList(propertyNames);
    }

    public String getFormName()
    {
        return "edit.build.properties";
    }

    public String[] getFieldNames()
    {
        List<String> fieldNames = new LinkedList<String>();
        fieldNames.add("revision");
        for (String propertyName : propertyNames)
        {
            fieldNames.add("property." + propertyName);
        }

        return fieldNames.toArray(new String[fieldNames.size()]);
    }

    /**
     * The first form argument is the revision, the remaining are the property values.
     * 
     * @param args the argument values
     */
    public void triggerFormElements(String... args)
    {
        submitFormElements("trigger", args);
    }
}
