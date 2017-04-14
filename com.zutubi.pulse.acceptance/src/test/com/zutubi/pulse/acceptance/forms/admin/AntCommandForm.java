/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.acceptance.windows.PulseFileSystemBrowserWindow;
import com.zutubi.pulse.core.commands.ant.AntCommandConfiguration;
import org.openqa.selenium.By;

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
        return (String) getFieldValue(FIELD_WORKING_DIR);
    }

    public String getBuildFileFieldValue()
    {
        return (String) getFieldValue(FIELD_BUILD_FILE);
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
        browser.click(By.id(getBrowseLinkId(FIELD_WORKING_DIR)));
        return new PulseFileSystemBrowserWindow(browser);
    }

    public PulseFileSystemBrowserWindow clickBrowseBuildFile()
    {
        browser.click(By.id(getBrowseLinkId(FIELD_BUILD_FILE)));
        return new PulseFileSystemBrowserWindow(browser);
    }
}
