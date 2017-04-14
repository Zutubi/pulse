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

package com.zutubi.pulse.acceptance.forms;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import org.openqa.selenium.By;

/**
 */
public class InstallPluginForm
{
    private static final String ID_PATH_FIELD = "zfid.pluginPath";
    private static final String ID_CONTINUE   = "zfid.continue";
    private static final String ID_CANCEL     = "zfid.cancel";

    private SeleniumBrowser browser;

    public InstallPluginForm(SeleniumBrowser browser)
    {
        this.browser = browser;
    }

    public boolean isFormPresent()
    {
        return browser.isElementIdPresent(ID_PATH_FIELD);
    }

    public void waitFor()
    {
        browser.waitForElement(ID_PATH_FIELD);
    }

    public void cancel()
    {
        browser.click(By.id(ID_CANCEL));
    }
    
    public void continueFormElements(String path)
    {
        browser.type(By.id(ID_PATH_FIELD), path);
        browser.click(By.id(ID_CONTINUE));
    }
}
