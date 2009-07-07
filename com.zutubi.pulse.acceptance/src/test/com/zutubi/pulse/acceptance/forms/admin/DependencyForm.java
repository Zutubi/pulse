package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;

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
}
