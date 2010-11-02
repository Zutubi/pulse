package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.admin.QuartzStatisticsPage;

public class QuartzStatisticsAcceptanceTest extends SeleniumTestBase
{
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        browser.loginAsAdmin();
    }

    public void testCanViewStatistics() throws Exception
    {
        final QuartzStatisticsPage statsPage = browser.openAndWaitFor(QuartzStatisticsPage.class);
        assertTrue(statsPage.isPresent());

        assertTrue(browser.isTextPresent("callback-triggers"));
    }
}
