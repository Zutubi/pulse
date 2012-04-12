package com.zutubi.pulse.acceptance.components;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import org.openqa.selenium.By;

/**
 * Corresponds to the Zutubi.TextBox component.
 */
public class TextBox extends Component
{
    public TextBox(SeleniumBrowser browser, String id)
    {
        super(browser, id);
    }

    @Override
    protected String getPresentScript()
    {
        return "return " + getComponentJS() + ".el.isDisplayed();";
    }

    /**
     * Returns the text displayed in the box.
     * 
     * @return the text displayed by this box
     */
    public String getText()
    {
        return browser.getText(By.id(id + "-text"));
    }
}