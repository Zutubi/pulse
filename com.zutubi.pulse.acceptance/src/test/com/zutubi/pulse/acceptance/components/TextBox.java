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

package com.zutubi.pulse.acceptance.components;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import org.openqa.selenium.By;

/**
 * Corresponds to the Zutubi.TextBox component.
 */
public class TextBox extends Component
{
    public TextBox(SeleniumBrowser browser, String id)
    {
        super(browser, id);
    }

    @Override
    protected String getPresentScript()
    {
        return "return " + getComponentJS() + ".el.isDisplayed();";
    }

    /**
     * Returns the text displayed in the box.
     * 
     * @return the text displayed by this box
     */
    public String getText()
    {
        return browser.getText(By.id(id + "-text"));
    }
}