package com.zutubi.pulse.acceptance.components;

import com.zutubi.pulse.acceptance.SeleniumBrowser;

/**
 * Base shared by all JS components.
 */
public abstract class Component
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
        return Boolean.valueOf(browser.evalExpression(getPresentExpression()));
    }

    /**
     * Waits for up to the given timeout for this component to be present.
     * 
     * @param timeout maximum time to wait in milliseconds
     */
    public void waitFor(long timeout)
    {
        browser.waitForCondition(getPresentExpression(), timeout);
    }

    /**
     * Returns a JS expression that can be evaluated to determine if the
     * component is present.  The default just checks for the component's main
     * element's existence.  Subclasses may override this if they need more
     * complex checks (e.g. for components that may be hidden).
     *  
     * @return a JS expression that evaluates to true if this component is
     *         present on the page
     */
    protected String getPresentExpression()
    {
        return SeleniumBrowser.CURRENT_WINDOW + ".Ext.getDom('" + id + "') != null";
    }

    /**
     * Returns a JS expression that can be used to get this component.
     * 
     * @return a JS expression that returns this component when evaluated
     */
    protected String getComponentJS()
    {
        return SeleniumBrowser.CURRENT_WINDOW + ".Ext.getCmp('" + id + "')";
    }
}
