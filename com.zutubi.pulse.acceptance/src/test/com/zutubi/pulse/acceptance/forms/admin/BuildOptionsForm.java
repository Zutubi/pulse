package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.tove.config.project.BuildOptionsConfiguration;

/**
 * The build options form, mapping to the BuildOptionsConfiguration class.
 */
public class BuildOptionsForm extends ConfigurationForm
{
    public BuildOptionsForm(SeleniumBrowser browser)
    {
        super(browser, BuildOptionsConfiguration.class);
    }

    public int[] getFieldTypes()
    {
        return new int[] { CHECKBOX, CHECKBOX, CHECKBOX, TEXTFIELD };
    }
}
