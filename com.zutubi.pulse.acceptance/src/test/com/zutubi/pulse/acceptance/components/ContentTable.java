package com.zutubi.pulse.acceptance.components;

import com.zutubi.pulse.acceptance.SeleniumBrowser;

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
}
