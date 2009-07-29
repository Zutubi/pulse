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

    public List<String> getProjectOptions()
    {
        return Arrays.asList(getComboBoxOptions("project"));
    }

    public void setProject(String projectHandle)
    {
        setFieldValue("project", projectHandle);

        // Ensure that actionInProgress is true before we wait for it to be false.  The ordering
        // may seem a little odd here, but it will ensure that we wait until the options have been
        // reloaded.
        browser.evalExpression("selenium.browserbot.getCurrentWindow().actionInProgress = true;");

        // Simulate the select event on the project field to that the subsequent
        // processes (update of the option field) is triggered.
        triggerEvent("project", "select");

        browser.waitForCondition("selenium.browserbot.getCurrentWindow().actionInProgress === false;");
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
