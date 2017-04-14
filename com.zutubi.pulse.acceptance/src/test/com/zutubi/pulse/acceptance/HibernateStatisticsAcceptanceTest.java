/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
