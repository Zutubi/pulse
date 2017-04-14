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

import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import org.openqa.selenium.By;

/**
 * The check form.
 */
public class CheckForm extends SeleniumForm
{
    private String name;

    public CheckForm(SeleniumForm mainForm)
    {
        super(mainForm.getSelenium());
        name = mainForm.getFormName() + "CheckHandler";
    }

    public String getFormName()
    {
        return name;
    }

    public String[] getFieldNames()
    {
        return new String[0];
    }

    public void checkFormElements(String... args)
    {
        submitFormElements("check", args);
    }

    public boolean isResultOk()
    {
        return (Boolean) browser.evaluateScript("return checkOK");
    }

    public String getResultMessage()
    {
        return browser.getText(By.id("check.result"));
    }

    public void waitForCheck()
    {
        browser.waitForVariable("checkComplete");
    }

    public void checkFormElementsAndWait(String... args)
    {
        checkFormElements(args);
        waitForCheck();
    }
}
