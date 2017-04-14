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
import com.zutubi.pulse.acceptance.components.Component;

/**
 * Corresponds to the Zutubi.table.ContentTable JS component base.
 */
public class ContentTable extends Component
{
    public ContentTable(SeleniumBrowser browser, String id)
    {
        super(browser, id);
    }
    
    protected String getPresentScript()
    {
        return "return " + getComponentJS() + ".dataExists();";
    }

    /**
     * Returns the HTML fragment that is the title of this table.
     * 
     * @return the title of this table
     */
    public String getTitle()
    {
        return (String) browser.evaluateScript("return " + getComponentJS() + ".title");
    }

    /**
     * Returns the length of the data array.
     * 
     * @return the data length
     */
    public long getDataLength()
    {
        return (Long)(browser.evaluateScript("return " + getComponentJS() + ".data.length;"));
    }

    /**
     * Returns the text content in the given cell.
     * 
     * @param row    zero-base row index
     * @param column zero-based column index
     * @return the contents of the given cell
     */
    public String getCellContents(int row, int column)
    {
        return browser.getCellContents(getId(), row, column);
    }
}
