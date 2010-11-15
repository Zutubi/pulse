package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.admin.QuartzStatisticsPage;

public class QuartzStatisticsAcceptanceTest extends AcceptanceTestBase
{
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        getBrowser().loginAsAdmin();
    }

    public void testCanViewStatistics() throws Exception
    {
        final QuartzStatisticsPage statsPage = getBrowser().openAndWaitFor(QuartzStatisticsPage.class);
        assertTrue(statsPage.isPresent());

        assertTrue(getBrowser().isTextPresent("callback-triggers"));
    }
}
