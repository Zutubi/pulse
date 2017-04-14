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
