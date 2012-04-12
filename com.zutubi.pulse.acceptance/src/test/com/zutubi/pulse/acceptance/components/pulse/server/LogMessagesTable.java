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
