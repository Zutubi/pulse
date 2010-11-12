package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

public class HibernateStatisticsPage extends SeleniumPage
{
    public HibernateStatisticsPage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, "hibernate-statistics-heading", "hibernate statistics");
    }

    public String getUrl()
    {
        return urls.admin() + "actions?hibernateStatistics=execute";
    }

    public boolean isEnabled()
    {
        return browser.getBodyText().contains("Hibernate statistics are enabled.");
    }

    public String getToggleId()
    {
        return "link.toggle";
    }

    public void clickToggleAndWait()
    {
        browser.click(getToggleId());
        browser.waitForPageToLoad();
    }
}
