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
