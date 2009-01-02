package com.zutubi.pulse.core.plugins;

import com.zutubi.pulse.core.test.api.PulseTestCase;

/**
 */
public class PluginVersionTest extends PulseTestCase
{
    public void testSimpleVersion()
    {
        PluginVersion v = new PluginVersion("1.2.3.v2006");
        assertEquals(1, v.getMajor());
        assertEquals(2, v.getMinor());
        assertEquals(3, v.getService());
        assertEquals("v2006", v.getQualifier());
    }

    public void testNoQualifier()
    {
        PluginVersion v = new PluginVersion("1.2.3");
        assertEquals(1, v.getMajor());
        assertEquals(2, v.getMinor());
        assertEquals(3, v.getService());
        assertNull(v.getQualifier());
    }

    public void testEmptyQualifier()
    {
        PluginVersion v = new PluginVersion("1.2.3.");
        assertEquals(1, v.getMajor());
        assertEquals(2, v.getMinor());
        assertEquals(3, v.getService());
        assertEquals("", v.getQualifier());
    }

    public void testMissingService()
    {
        try
        {
            PluginVersion v = new PluginVersion("1.2");
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
            PluginVersion v = new PluginVersion("1");
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
            PluginVersion v = new PluginVersion("");
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Version contains less than three segments", e.getMessage());
        }
    }

    public void testCompareIdentical()
    {
        PluginVersion v1 = new PluginVersion("1.2.3.v20070111");
        PluginVersion v2 = new PluginVersion("1.2.3.v20070111");
        assertTrue(v1.equals(v2));
        assertTrue(v1.compareTo(v2) == 0);
    }

    public void testCompareOneQualifier()
    {
        PluginVersion v1 = new PluginVersion("1.2.3.v20070111");
        PluginVersion v2 = new PluginVersion("1.2.3");
        assertTrue(v1.equals(v2));
        assertTrue(v1.compareTo(v2) == 0);
        assertTrue(v2.compareTo(v1) == 0);
    }

    public void testCompareNoQualifiers()
    {
        PluginVersion v1 = new PluginVersion("1.2.3");
        PluginVersion v2 = new PluginVersion("1.2.3");
        assertTrue(v1.equals(v2));
        assertTrue(v1.compareTo(v2) == 0);
    }
    
    public void testCompareQualifierChange()
    {
        PluginVersion v1 = new PluginVersion("1.2.3.v20070111");
        PluginVersion v2 = new PluginVersion("1.2.3.v20070112");
        assertFalse(v1.equals(v2));
        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    public void testCompareServiceChange()
    {
        PluginVersion v1 = new PluginVersion("1.2.3");
        PluginVersion v2 = new PluginVersion("1.2.10");
        assertFalse(v1.equals(v2));
        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    public void testCompareMinorChange()
    {
        PluginVersion v1 = new PluginVersion("1.2.3");
        PluginVersion v2 = new PluginVersion("1.7777.3");
        assertFalse(v1.equals(v2));
        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    public void testCompareMajorChange()
    {
        PluginVersion v1 = new PluginVersion("1.2.3");
        PluginVersion v2 = new PluginVersion("3.2.3");
        assertFalse(v1.equals(v2));
        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    public void testToString()
    {
        PluginVersion v = new PluginVersion("1.2.3.v20089");
        assertEquals("1.2.3.v20089", v.toString());
    }

    public void testToStringEmtpyQualifier()
    {
        PluginVersion v = new PluginVersion("1.2.3.");
        assertEquals("1.2.3", v.toString());
    }
    
    public void testToStringNoQualifier()
    {
        PluginVersion v = new PluginVersion("1.2.3");
        assertEquals("1.2.3", v.toString());
    }
}
