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
