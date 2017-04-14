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

package com.zutubi.pulse.acceptance.components.pulse.server;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.Component;
import org.openqa.selenium.By;

/**
 * Corresponds to the Zutubi.pulse.server.LogMessagesTable JS component.
 */
public class LogMessagesTable extends Component
{
    public LogMessagesTable(SeleniumBrowser browser, String id)
    {
        super(browser, id);
    }
    
    protected String getPresentScript()
    {
        return "return " + getComponentJS() + ".dataExists();";
    }

    /**
     * Returns the number of entries shown.
     * 
     * @return the entry count
     */
    public long getEntryCount()
    {
        return (Long) browser.evaluateScript("return " + getComponentJS() + ".data.length");
    }

    /**
     * Returns the text content in the given cell.
     * 
     * @param index zero-base entry index
     * @return the message for the given entry
     */
    public String getEntryMessage(int index)
    {
        return browser.getText(By.id(getId() + "-message-" + index));
    }
}
