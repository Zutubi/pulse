package com.zutubi.util;

import com.zutubi.util.junit.ZutubiTestCase;

public class EnumUtilsTest extends ZutubiTestCase
{
    public enum SampleEnum
    {
        NAME, NAME_WITH_UNDERSCORES
    }

    public void testToPrettyString()
    {
        assertEquals("name", EnumUtils.toPrettyString(SampleEnum.NAME));
        assertEquals("name with underscores", EnumUtils.toPrettyString(SampleEnum.NAME_WITH_UNDERSCORES));
    }

    public void testToString()
    {
        assertEquals("name", EnumUtils.toString(SampleEnum.NAME));
        assertEquals("namewithunderscores", EnumUtils.toString(SampleEnum.NAME_WITH_UNDERSCORES));
    }

    public void testFromPrettyString()
    {
        assertEquals("NAME", EnumUtils.fromPrettyString("name"));
        assertEquals("NAME_WITH_UNDERSCORES", EnumUtils.fromPrettyString("name with underscores"));
    }

    public void testEnumFromPrettyString()
    {
        assertEquals(SampleEnum.NAME, EnumUtils.fromPrettyString(SampleEnum.class, "name"));        
        assertEquals(SampleEnum.NAME_WITH_UNDERSCORES, EnumUtils.fromPrettyString(SampleEnum.class, "name with underscores"));
    }
}
