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

package com.zutubi.pulse.acceptance.forms.setup;

import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.windows.PulseFileSystemBrowserWindow;
import com.zutubi.pulse.master.tove.config.setup.SetupDataConfiguration;
import org.openqa.selenium.By;

public class SetPulseDataForm extends SeleniumForm
{
    private static final String FIELD_DATA = "data";

    public SetPulseDataForm(SeleniumBrowser browser)
    {
        super(browser, false);
    }

    public String getFormName()
    {
        return SetupDataConfiguration.class.getName();
    }

    public String[] getFieldNames()
    {
        return new String[]{FIELD_DATA};
    }

    public boolean isBrowseDataLinkPresent()
    {
        return browser.isElementIdPresent(getBrowseLinkId(FIELD_DATA));
    }

    public PulseFileSystemBrowserWindow clickBrowseData()
    {
        browser.click(By.id(getBrowseLinkId(FIELD_DATA)));
        return new PulseFileSystemBrowserWindow(browser);
    }
}
