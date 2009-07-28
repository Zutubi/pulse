package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.util.StringUtils;

import java.util.Arrays;
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

    public boolean isProjectInOptions(String projectHandle)
    {
        List<String> options = Arrays.asList(getComboBoxOptions("project"));
        return options.contains(projectHandle);
    }

    public void setProject(String projectHandle)
    {
        setFieldValue("project", projectHandle);

        // Simulate the select event on the project field to that the subsequent
        // processes (update of the option field) is triggered.
        triggerEvent("project", "select");

        // The 'select' event triggers an ajax refresh of the stage fields options.  Wait for
        // this refresh to complete before continueing.
//        waitForAjaxCallToComplete();
        // it seems that this ajax wait is only for the loading, and does not include the success/failure
        // function calls.
        try
        {
            // what is the best way to fix this?...
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            // noop.
        }
    }

    /**
     * Wait for any pending Ext.Ajax calls to complete.
     */
    public void waitForAjaxCallToComplete()
    {
        browser.waitForCondition("selenium.browserbot.getCurrentWindow().Ext.Ajax.isLoading();");
    }

    public Object getStagesValues()
    {
        return Arrays.asList(getComboBoxOptions("stages"));
    }

    public List<String> getStagesOptionValues()
    {
        String keys = browser.evalExpression("selenium.browserbot.getCurrentWindow().Ext.getCmp('zfid.stages').getOptionValues();");
        return Arrays.asList(StringUtils.split(keys, ',', true));
    }
}
