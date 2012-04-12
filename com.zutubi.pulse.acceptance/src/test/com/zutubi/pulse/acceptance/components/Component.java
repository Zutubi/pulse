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
        return (Boolean) browser.evaluateScript(getPresentScript());
    }

    /**
     * Waits for up to the given timeout for this component to be present.
     * 
     * @param timeout maximum time to wait in milliseconds
     */
    public void waitFor(long timeout)
    {
        browser.waitForCondition(getPresentScript(), timeout);
    }

    /**
     * Returns a JS script that can be evaluated to determine if the component
     * is present.  The default just checks for the component's main element's
     * existence.  Subclasses may override this if they need more complex
     * checks (e.g. for components that may be hidden).
     *  
     * @return a JS script that returns true if this component is present on
     *         the page (it must return a boolean value)
     */
    protected String getPresentScript()
    {
        return "return Ext.getDom('" + id + "') != null";
    }

    /**
     * Returns a JS expression that can be used to get this component.
     * 
     * @return a JS expression that returns this component when evaluated
     */
    protected String getComponentJS()
    {
        return "Ext.getCmp('" + id + "')";
    }
}
