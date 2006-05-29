package com.zutubi.pulse.license;

import com.zutubi.pulse.test.PulseTestCase;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * <class-comment/>
 */
public class LicenseTest extends PulseTestCase
{
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public LicenseTest()
    {
    }

    public LicenseTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testCalculateDaysRemaining() throws ParseException
    {
        // check the day of expiry.
        assertRemaining(1, "2006-04-01 00:01:01", "2006-04-01 01:01:01");
        assertRemaining(1, "2006-04-01 01:01:01", "2006-04-01 01:01:01");
        assertRemaining(1, "2006-04-01 23:01:01", "2006-04-01 01:01:01");

        // check the day after the expiry.
        assertRemaining(0, "2006-04-02 00:00:00", "2006-04-01 01:01:01");

        // check the boundry case for expiry.
        assertRemaining(2, "2006-04-01 00:00:00", "2006-04-02 00:00:00");
        assertRemaining(1, "2006-04-02 00:00:00", "2006-04-02 00:00:00");
        assertRemaining(0, "2006-04-03 00:00:00", "2006-04-02 00:00:00");

        // check that things are correct in the long term.
        assertRemaining(366, "2005-04-02 00:00:00", "2006-04-02 00:00:00");

        // check the year changeover.
        assertRemaining(1, "2006-01-01 00:00:00", "2006-01-01 00:00:01");
        assertRemaining(1, "2006-01-01 00:00:00", "2006-01-01 00:00:00");
        assertRemaining(0, "2006-01-01 00:00:00", "2005-12-31 23:59:59");

        assertRemaining(2, "2005-12-31 23:59:59", "2006-01-01 00:00:00");
        assertRemaining(1, "2005-12-31 23:59:59", "2005-12-31 23:59:59");
    }

    private void assertRemaining(int daysRemaining, String now, String expiry) throws ParseException
    {
        assertEquals(daysRemaining, License.calculateDaysRemaining(DATE_FORMAT.parse(now), DATE_FORMAT.parse(expiry)));
    }
}
