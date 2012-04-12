package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.acceptance.windows.PulseFileSystemBrowserWindow;
import com.zutubi.pulse.master.tove.config.project.types.VersionedTypeConfiguration;
import org.openqa.selenium.By;

public class ConvertToVersionedForm extends ConfigurationForm
{
    private static final String FIELD_PULSE_FILE_NAME = "pulseFileName";

    public ConvertToVersionedForm(SeleniumBrowser browser)
    {
        super(browser, VersionedTypeConfiguration.class);
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD};
    }

    @Override
    public String[] getFieldNames()
    {
        return new String[]{FIELD_PULSE_FILE_NAME};
    }

    public boolean isBrowsePulseFileNameLinkAvailable()
    {
        return browser.isElementIdPresent(getBrowseLinkId(FIELD_PULSE_FILE_NAME));
    }

    public PulseFileSystemBrowserWindow clickBrowsePulseFileName()
    {
        browser.click(By.id(getBrowseLinkId(FIELD_PULSE_FILE_NAME)));
        return new PulseFileSystemBrowserWindow(browser);
    }

    public String getPulseFileNameFieldValue()
    {
        return (String) getFieldValue(FIELD_PULSE_FILE_NAME);
    }
}
