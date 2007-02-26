package com.zutubi.pulse.plugins;

import com.zutubi.pulse.test.PulseTestCase;

/**
 */
public class VersionTest extends PulseTestCase
{
    public void testSimpleVersion()
    {
        Version v = new Version("1.2.3.v2006");
        assertEquals(1, v.getMajor());
        assertEquals(2, v.getMinor());
        assertEquals(3, v.getService());
        assertEquals("v2006", v.getQualifier());
    }

    public void testNoQualifier()
    {
        Version v = new Version("1.2.3");
        assertEquals(1, v.getMajor());
        assertEquals(2, v.getMinor());
        assertEquals(3, v.getService());
        assertNull(v.getQualifier());
    }

    public void testEmptyQualifier()
    {
        Version v = new Version("1.2.3.");
        assertEquals(1, v.getMajor());
        assertEquals(2, v.getMinor());
        assertEquals(3, v.getService());
        assertEquals("", v.getQualifier());
    }

    public void testMissingService()
    {
        try
        {
            Version v = new Version("1.2");
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Version contains less than three segments", e.getMessage());
        }
    }

    public void testMissingMinor()
    {
        try
        {
            Version v = new Version("1");
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Version contains less than three segments", e.getMessage());
        }
    }
    
    public void testEmpty()
    {
        try
        {
            Version v = new Version("");
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Version contains less than three segments", e.getMessage());
        }
    }

    public void testCompareIdentical()
    {
        Version v1 = new Version("1.2.3.v20070111");
        Version v2 = new Version("1.2.3.v20070111");
        assertTrue(v1.equals(v2));
        assertTrue(v1.compareTo(v2) == 0);
    }

    public void testCompareOneQualifier()
    {
        Version v1 = new Version("1.2.3.v20070111");
        Version v2 = new Version("1.2.3");
        assertTrue(v1.equals(v2));
        assertTrue(v1.compareTo(v2) == 0);
        assertTrue(v2.compareTo(v1) == 0);
    }

    public void testCompareNoQualifiers()
    {
        Version v1 = new Version("1.2.3");
        Version v2 = new Version("1.2.3");
        assertTrue(v1.equals(v2));
        assertTrue(v1.compareTo(v2) == 0);
    }
    
    public void testCompareQualifierChange()
    {
        Version v1 = new Version("1.2.3.v20070111");
        Version v2 = new Version("1.2.3.v20070112");
        assertFalse(v1.equals(v2));
        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    public void testCompareServiceChange()
    {
        Version v1 = new Version("1.2.3");
        Version v2 = new Version("1.2.10");
        assertFalse(v1.equals(v2));
        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    public void testCompareMinorChange()
    {
        Version v1 = new Version("1.2.3");
        Version v2 = new Version("1.7777.3");
        assertFalse(v1.equals(v2));
        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    public void testCompareMajorChange()
    {
        Version v1 = new Version("1.2.3");
        Version v2 = new Version("3.2.3");
        assertFalse(v1.equals(v2));
        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    public void testToString()
    {
        Version v = new Version("1.2.3.v20089");
        assertEquals("1.2.3.v20089", v.toString());
    }

    public void testToStringEmtpyQualifier()
    {
        Version v = new Version("1.2.3.");
        assertEquals("1.2.3", v.toString());
    }
    
    public void testToStringNoQualifier()
    {
        Version v = new Version("1.2.3");
        assertEquals("1.2.3", v.toString());
    }
}
