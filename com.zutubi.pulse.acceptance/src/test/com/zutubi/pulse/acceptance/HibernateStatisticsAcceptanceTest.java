package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.admin.HibernateStatisticsPage;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.util.Condition;

/**
 * A sanity check to ensure that the hibernate statistics are displayable.
 */
public class HibernateStatisticsAcceptanceTest extends AcceptanceTestBase
{
    private static final int TIMEOUT = 60000;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        getBrowser().loginAsAdmin();
    }

    public void testCanViewStatistics() throws Exception
    {
        final HibernateStatisticsPage statsPage = getBrowser().openAndWaitFor(HibernateStatisticsPage.class);
        assertTrue(statsPage.isPresent());
        assertFalse(statsPage.isEnabled());

        statsPage.clickToggle();
        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return statsPage.isEnabled();
            }
        }, TIMEOUT, "statistics to be enabled");

        statsPage.clickToggle();
        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return !statsPage.isEnabled();
            }
        }, TIMEOUT, "statistics to be disabled");
    }

}
