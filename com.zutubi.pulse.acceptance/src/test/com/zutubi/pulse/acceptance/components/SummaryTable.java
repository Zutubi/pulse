package com.zutubi.pulse.acceptance.components;

import com.zutubi.pulse.acceptance.SeleniumBrowser;

/**
 * Corresponds to the Zutubi.table.SummaryTable JS component.
 */
public class SummaryTable extends Component
{
    public SummaryTable(SeleniumBrowser browser, String id)
    {
        super(browser, id);
    }

    /**
     * Returns the number of data rows displayed by this table.
     *
     * @return the number of data rows shown
     */
    public int getRowCount()
    {
        return Integer.parseInt(browser.evalExpression(getComponentJS() + ".data.length"));
    }
}