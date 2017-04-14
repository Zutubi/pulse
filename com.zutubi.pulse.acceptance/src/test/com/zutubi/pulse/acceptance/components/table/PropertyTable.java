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

package com.zutubi.pulse.acceptance.components.table;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import org.openqa.selenium.By;

/**
 * Corresponds to the Zutubi.table.PropertyTable component.
 */
public class PropertyTable extends ContentTable
{
    public PropertyTable(SeleniumBrowser browser, String id)
    {
        super(browser, id);
    }

    /**
     * Indicates if a row with the given name is present.
     *
     * @param rowName name of the row to check for
     * @return true if the row is present, false otherwise
     */
    public boolean isRowPresent(String rowName)
    {
        return browser.isElementIdPresent(getValueCellId(rowName));
    }

    /**
     * Returns the DOM id of the table cell holding the value for the row with
     * the given name.
     *
     * @param rowName name of the row
     * @return DOM id for the value cell in the given row
     */
    public String getValueCellId(String rowName)
    {
        return getId() + "-" + rowName;
    }

    /**
     * Gets the value for the given row (as the text from the corresponding
     * cell).
     *
     * @param rowName name of the row to get the value for
     * @return text in the value cell for the row of the given name
     */
    public String getValue(String rowName)
    {
        return browser.getText(By.id(getValueCellId(rowName)));
    }
}
