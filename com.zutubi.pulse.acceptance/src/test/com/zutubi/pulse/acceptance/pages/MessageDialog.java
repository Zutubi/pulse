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

package com.zutubi.pulse.acceptance.pages;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import org.openqa.selenium.By;

/**
 * Base class for dialogs created by Ext.
 */
public class MessageDialog
{
    private static final String SELECTOR_INPUT_SINGLE = "input.ext-mb-input";
    private static final String SELECTOR_INPUT_MULTI = "textarea.ext-mb-textarea";
    private static final String XPATH_FORMAT_BUTTON = "//button[contains(text(),'%s')]";

    private SeleniumBrowser browser;
    private boolean multiline;
    private String affirmButton = "ok";
    private String declineButton = "cancel";

    public MessageDialog(SeleniumBrowser browser, boolean multiline)
    {
        this.browser = browser;
        this.multiline = multiline;
    }

    public MessageDialog(SeleniumBrowser browser, boolean multiline, String affirmButton, String declineButton)
    {
        this.browser = browser;
        this.multiline = multiline;
        this.affirmButton = affirmButton;
        this.declineButton = declineButton;
    }

    public void waitFor()
    {
        browser.waitForElement(By.xpath(String.format(XPATH_FORMAT_BUTTON, affirmButton)));
    }

    public boolean isVisible()
    {
        return browser.isVisible(By.xpath(String.format(XPATH_FORMAT_BUTTON, affirmButton)));
    }

    public void typeInput(String text)
    {
        browser.type(By.cssSelector(getInputSelector()), text);
    }

    private String getInputSelector()
    {
        return multiline ? SELECTOR_INPUT_MULTI : SELECTOR_INPUT_SINGLE;
    }

    public void clickAffirm()
    {
        browser.click(By.xpath(String.format(XPATH_FORMAT_BUTTON, affirmButton)));
    }

    public void clickDecline()
    {
        browser.click(By.xpath(String.format(XPATH_FORMAT_BUTTON, declineButton)));
    }
}