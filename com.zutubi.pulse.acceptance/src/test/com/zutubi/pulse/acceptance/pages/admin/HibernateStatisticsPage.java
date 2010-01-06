package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

public class HibernateStatisticsPage extends SeleniumPage
{
    public HibernateStatisticsPage(Selenium selenium, Urls urls)
    {
        super(selenium, urls, "hibernate.statistics.heading", "hibernate statistics");
    }

    public String getUrl()
    {
        return urls.admin() + "actions?hibernateStatistics=execute";
    }

    public boolean isEnabled()
    {
        return selenium.getBodyText().contains("Hibernate statistics are enabled.");
    }

    public String getToggleId()
    {
        return "link.toggle";
    }

    public void clickToggle()
    {
        selenium.click(getToggleId());
    }
}
