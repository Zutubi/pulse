package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.acceptance.windows.PulseFileSystemBrowserWindow;
import com.zutubi.pulse.core.commands.ant.AntCommandConfiguration;

/**
 * Form for configuration of ant commands.
 */
public class AntCommandForm extends ConfigurationForm
{
    private static final String FIELD_WORKING_DIR = "workingDir";
    private static final String FIELD_BUILD_FILE = "buildFile";

    public AntCommandForm(SeleniumBrowser browser)
    {
        super(browser, AntCommandConfiguration.class);
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, ITEM_PICKER, ITEM_PICKER, TEXTFIELD, TEXTFIELD, TEXTFIELD, CHECKBOX };
    }

    public String getWorkingDirectoryFieldValue()
    {
        return getFieldValue(FIELD_WORKING_DIR);
    }

    public String getBuildFileFieldValue()
    {
        return getFieldValue(FIELD_BUILD_FILE);
    }

    public boolean isBrowseWorkingDirectoryLinkPresent()
    {
        return browser.isElementIdPresent(getBrowseLinkId(FIELD_WORKING_DIR));
    }

    public boolean isBrowseBuildFileLinkPresent()
    {
        return browser.isElementIdPresent(getBrowseLinkId(FIELD_BUILD_FILE));
    }

    public PulseFileSystemBrowserWindow clickBrowseWorkingDirectory()
    {
        browser.click(getBrowseLinkId(FIELD_WORKING_DIR));
        return new PulseFileSystemBrowserWindow(browser);
    }

    public PulseFileSystemBrowserWindow clickBrowseBuildFile()
    {
        browser.click(getBrowseLinkId(FIELD_BUILD_FILE));
        return new PulseFileSystemBrowserWindow(browser);
    }
}
