package com.zutubi.pulse.acceptance.components.pulse.server;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.Component;

/**
 * Corresponds to the Zutubi.pulse.server.LogMessagesTable JS component.
 */
public class LogMessagesTable extends Component
{
    public LogMessagesTable(SeleniumBrowser browser, String id)
    {
        super(browser, id);
    }
    
    protected String getPresentExpression()
    {
        return getComponentJS() + ".dataExists()";
    }

    /**
     * Returns the number of entries shown.
     * 
     * @return the entry count
     */
    public int getEntryCount()
    {
        return Integer.parseInt(browser.evalExpression(getComponentJS() + ".data.length"));
    }

    /**
     * Returns the text content in the given cell.
     * 
     * @param index zero-base entry index
     * @return the message for the given entry
     */
    public String getEntryMessage(int index)
    {
        return browser.getText(getId() + "-message-" + index);
    }
}
