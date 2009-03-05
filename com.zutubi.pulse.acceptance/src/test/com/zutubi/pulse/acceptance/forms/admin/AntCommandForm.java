package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.acceptance.windows.BrowseScmWindow;
import com.zutubi.pulse.core.commands.ant.AntCommandConfiguration;

/**
 * Form for configuration of ant commands.
 */
public class AntCommandForm extends ConfigurationForm
{
    private static final String FIELD_WORKING_DIR = "workingDir";
    private static final String FIELD_BUILD_FILE = "buildFile";

    public AntCommandForm(Selenium selenium)
    {
        super(selenium, AntCommandConfiguration.class);
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
        return selenium.isElementPresent(getBrowseLinkId(FIELD_WORKING_DIR));
    }

    public boolean isBrowseBuildFileLinkPresent()
    {
        return selenium.isElementPresent(getBrowseLinkId(FIELD_BUILD_FILE));
    }

    public BrowseScmWindow clickBrowseWorkingDirectory()
    {
        selenium.click(getBrowseLinkId(FIELD_WORKING_DIR));
        BrowseScmWindow window = new BrowseScmWindow(selenium);
        window.selectWindow();
        return window;
    }

    public BrowseScmWindow clickBrowseBuildFile()
    {
        selenium.click(getBrowseLinkId(FIELD_BUILD_FILE));
        BrowseScmWindow window = new BrowseScmWindow(selenium);
        window.selectWindow();
        return window;
    }

    private String getBrowseLinkId(String field)
    {
        return getFieldId(field) + ".browse";
    }
}
