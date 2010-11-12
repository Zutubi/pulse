package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

public class QuartzStatisticsPage extends SeleniumPage
{
    public QuartzStatisticsPage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, "quartz-statistics-heading");
    }

    @Override
    public String getUrl()
    {
        return urls.admin() + "actions?quartzStatistics=execute";
    }
}
