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
        int count = getKeyValuePairCount();
        Map<String, String> values = new HashMap<String, String>();
        for (int i = 1; i <= count; i++)
        {
            values.put(browser.getCellContents(id, i, 0), browser.getCellContents(id, i, 1));
        }
        
        return values;
    }

    private int getKeyValuePairCount()
    {
        return Integer.parseInt(browser.evalExpression(getComponentJS() + ".getCount()"));
    }
}
