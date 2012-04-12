package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;

import java.util.List;

/**
 * The dependency form
 */
public class DependencyForm extends ConfigurationForm
{
    public DependencyForm(SeleniumBrowser browser)
    {
        super(browser, DependencyConfiguration.class);
    }

    public List<String> getProjectOptions()
    {
        return getComboBoxOptions("project");
    }

    public void setProject(String projectHandle)
    {
        setFieldValue("project", projectHandle);

        // Simulate the select event on the project field to that the subsequent
        // processes (update of the option field) is triggered.
        triggerEvent("project", "select");

        browser.waitForCondition("return actionInProgress === false;");
    }

    public Object getStagesValues()
    {
        return getComboBoxOptions("stages");
    }

    @SuppressWarnings("unchecked")
    public List<String> getStagesOptionValues()
    {
        return (List<String>) browser.evaluateScript("return Ext.getCmp('zfid.stages').getOptionValues();");
    }
}
