package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.acceptance.windows.BrowseScmWindow;
import com.zutubi.pulse.master.tove.config.project.types.AntTypeConfiguration;
import junit.framework.TestCase;

/**
 * The ant project type form.
 */
public class AntTypeForm extends ConfigurationForm
{
    public AntTypeForm(Selenium selenium)
    {
        super(selenium, AntTypeConfiguration.class);
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, MULTI_SELECT };
    }

    public String getWorkingDirectoryFieldValue()
    {
        return getFieldValue("work");
    }

    public String getBuildFileFieldValue()
    {
        return getFieldValue("file");
    }

    public void assertBrowseWorkingDirectoryLinkPresent()
    {
        TestCase.assertTrue(isBrowseWorkingDirectoryLinkPresent());
    }

    public void assertBrowseWorkingDirectoryLinkNotPresent()
    {
        TestCase.assertFalse(isBrowseWorkingDirectoryLinkPresent());
    }

    private boolean isBrowseWorkingDirectoryLinkPresent()
    {
        return selenium.isElementPresent(getBrowseLinkId("work"));
    }

    public void assertBrowseBuildFileLinkPresent()
    {
        TestCase.assertTrue(isBrowseBuildFileLinkPresent());
    }

    public void assertBrowseBuildFileLinkNotPresent()
    {
        TestCase.assertFalse(isBrowseBuildFileLinkPresent());
    }

    private boolean isBrowseBuildFileLinkPresent()
    {
        return selenium.isElementPresent(getBrowseLinkId("file"));
    }

    public BrowseScmWindow clickBrowseWorkingDirectory()
    {
        selenium.click(getBrowseLinkId("work"));
        BrowseScmWindow window = new BrowseScmWindow(selenium);
        window.selectWindow();
        return window;
    }

    public BrowseScmWindow clickBrowseBuildFile()
    {
        selenium.click(getBrowseLinkId("file"));
        BrowseScmWindow window = new BrowseScmWindow(selenium);
        window.selectWindow();
        return window;
    }

    private String getBrowseLinkId(String field)
    {
        return getFieldId(field) + ".browse";
    }
}
