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
    
    protected String getPresentExpression()
    {
        return getComponentJS() + ".dataExists()";
    }

    /**
     * Returns the HTML fragment that is the title of this table.
     * 
     * @return the title of this table
     */
    public String getTitle()
    {
        return browser.evalExpression(getComponentJS() + ".title");
    }
}
