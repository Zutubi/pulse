package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.tove.config.project.types.CustomTypeConfiguration;

/**
 * A text area to edit the pulse file.
 */
public class CustomTypeForm extends ConfigurationForm
{
    public CustomTypeForm(SeleniumBrowser browser)
    {
        super(browser, CustomTypeConfiguration.class);
    }

    public String[] getFieldNames()
    {
        return new String[]{"pulseFileString"};
    }
}
