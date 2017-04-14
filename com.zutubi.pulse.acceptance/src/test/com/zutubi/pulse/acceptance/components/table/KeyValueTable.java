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

import java.util.HashMap;
import java.util.Map;

/**
 * Corresponds to the Zutubi.table.KeyValueTable JS component.
 */
public class KeyValueTable extends ContentTable
{
    public KeyValueTable(SeleniumBrowser browser, String id)
    {
        super(browser, id);
    }

    public Map<String, String> getKeyValuePairs()
    {
        // Expand to make all rows visible, allowing us to get the text
        expand();
        
        long count = getKeyValuePairCount();
        Map<String, String> values = new HashMap<String, String>();
        for (int i = 1; i <= count; i++)
        {
            values.put(browser.getCellContents(id, i, 0), browser.getCellContents(id, i, 1));
        }
        
        return values;
    }
    
    public void expand()
    {
        browser.evaluateScript(getComponentJS() + ".expand()");
    }

    private long getKeyValuePairCount()
    {
        return (Long) browser.evaluateScript("return " + getComponentJS() + ".getCount();");
    }
}
