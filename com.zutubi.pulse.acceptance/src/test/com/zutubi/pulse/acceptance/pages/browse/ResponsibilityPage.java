package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * Abstract base for pages that show the user responsible for a build.
 */
public abstract class ResponsibilityPage extends SeleniumPage
{
    private static final String ID_RESPONSIBLE_PANEL = "responsible-panel";
    private static final String ID_RESPONSIBLE_MESSAGE = "responsible-message";
    private static final String ID_RESPONSIBLE_CLEAR = "responsible-clear";
    private static final String ID_RESPONSIBLE_COMMENT = "responsible-comment";

    public ResponsibilityPage(Selenium selenium, Urls urls, String id, String title)
    {
        super(selenium, urls, id, title);
    }

    public boolean hasResponsibleUser()
    {
        return selenium.isElementPresent(ID_RESPONSIBLE_PANEL);
    }

    public String getResponsibleMessage()
    {
        return selenium.getText(ID_RESPONSIBLE_MESSAGE);
    }

    public String getResponsibleComment()
    {
        if (selenium.isElementPresent(ID_RESPONSIBLE_COMMENT))
        {
            return selenium.getText(ID_RESPONSIBLE_COMMENT);
        }
        else
        {
            return "";
        }
    }

    public void clickClearResponsible()
    {
        selenium.click(ID_RESPONSIBLE_CLEAR);
    }

    public boolean isClearResponsibilityPresent()
    {
        return selenium.isElementPresent(ID_RESPONSIBLE_CLEAR);
    }

    private String getActionId(String actionName)
    {
        return "action." + actionName;
    }

    public boolean isActionPresent(String actionName)
    {
        return selenium.isElementPresent(getActionId(actionName));
    }

    public void clickAction(String actionName)
    {
        selenium.click(getActionId(actionName));
    }
}
