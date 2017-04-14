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

package com.zutubi.pulse.acceptance.components.pulse.project;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.Component;
import org.openqa.selenium.By;

/**
 * Corresponds to the Zutubi.pulse.project.StatusBox component.
 */
public class StatusBox extends Component
{
    public StatusBox(SeleniumBrowser browser, String id)
    {
        super(browser, id);
    }

    /**
     * Returns the title at the top of the box.
     *
     * @return the box title
     */
    public String getTitle()
    {
        return browser.getText(By.id(getId() + "-title"));
    }

    /**
     * Indicates if a property with the given name is present.
     *
     * @param propertyName name of the property to check for
     * @return true if the property  is present, false otherwise
     */
    public boolean isPropertyPresent(String propertyName)
    {
        return browser.isElementIdPresent(getValueId(propertyName));
    }

    /**
     * Returns the DOM id of the table cell holding the value for the property
     * with the given name.
     *
     * @param propertyName name of the property
     * @return DOM id for the value of the given property
     */
    public String getValueId(String propertyName)
    {
        return getId() + "-" + propertyName;
    }

    /**
     * Gets the value for the given property (as raw text).
     *
     * @param propertyName name of the property to get the value for
     * @return text for the given property
     */
    public String getValue(String propertyName)
    {
        return browser.getText(By.id(getValueId(propertyName)));
    }
}