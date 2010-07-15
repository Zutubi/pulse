package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.tove.config.project.ProjectTypeSelectionConfiguration;

/**
 * Form for the special type-selection state in the project wizard.
 */
public class ProjectTypeSelectState extends ConfigurationForm
{
    public ProjectTypeSelectState(SeleniumBrowser browser)
    {
        super(browser, ProjectTypeSelectionConfiguration.class);
    }
}
