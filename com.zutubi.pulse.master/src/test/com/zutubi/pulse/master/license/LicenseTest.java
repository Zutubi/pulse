package com.zutubi.pulse.master.license;

import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LicenseTest extends PulseTestCase
{
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void testExpires()
    {
        License a = new License(LicenseType.EVALUATION, "holder", null);
        assertFalse(a.expires());
        License b = new License(LicenseType.EVALUATION, "holder", new Date());
        assertTrue(b.expires());
    }

    public void testLicenseTypeInEqualsMethod()
    {
        License a = new License(LicenseType.EVALUATION, "holder", null);
        License b = new License(LicenseType.EVALUATION, "holder", null);
        License c = new License(LicenseType.STANDARD, "holder", null);

        assertTrue(a.equals(b));
        assertFalse(a.equals(c));
    }

    public void testSupportedValuesInEqualsMethod()
    {
        License a = new License(LicenseType.EVALUATION, "holder", null);
        License b = new License(LicenseType.EVALUATION, "holder", null);

        // check agents.
        assertTrue(a.equals(b));
        a.setSupportedAgents(1);
        assertFalse(a.equals(b));
        b.setSupportedAgents(1);
        assertTrue(a.equals(b));

        // check projects
        assertTrue(a.equals(b));
        a.setSupportedProjects(2);
        assertFalse(a.equals(b));
        b.setSupportedProjects(2);
        assertTrue(a.equals(b));

        // check users.
        assertTrue(a.equals(b));
        a.setSupportedUsers(5);
        assertFalse(a.equals(b));
        b.setSupportedUsers(5);
        assertTrue(a.equals(b));
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
        assertRemaining(2, "2005-04-01 00:00:00", "2005-04-02 00:00:00");
        assertRemaining(1, "2005-04-02 00:00:00", "2005-04-02 00:00:00");
        assertRemaining(0, "2005-04-03 00:00:00", "2005-04-02 00:00:00");

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
        assertEquals(daysRemaining, License.calculateDaysRemaining(dateFormat.parse(now), dateFormat.parse(expiry)));
    }
}
