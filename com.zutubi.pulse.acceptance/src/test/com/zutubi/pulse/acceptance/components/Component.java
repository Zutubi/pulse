package com.zutubi.pulse.acceptance.components;

import com.zutubi.pulse.acceptance.SeleniumBrowser;

/**
 * Base shared by all JS components.
 */
public class Component
{
    protected SeleniumBrowser browser;
    protected String id;

    public Component(SeleniumBrowser browser, String id)
    {
        this.browser = browser;
        this.id = id;
    }

    /**
     * Returns the DOM id for this component's main element.
     *
     * @return the DOM id for this component
     */
    public String getId()
    {
        return id;
    }

    /**
     * Indicates if this component is present on the page.
     *
     * @return true if this component is present, false otherwise
     */
    public boolean isPresent()
    {
        return browser.isElementIdPresent(id);
    }

    protected String getComponentJS()
    {
        return "selenium.browserbot.getCurrentWindow().Ext.getCmp('" + id + "')";
    }
}
