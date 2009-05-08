package com.zutubi.util;

import com.zutubi.util.junit.ZutubiTestCase;

public class UnitTest extends ZutubiTestCase
{
    public void testParse()
    {
        assertEquals(Unit.DAY, Unit.parse("day"));
        assertEquals(Unit.DAY, Unit.parse("Day"));
        assertEquals(Unit.DAY, Unit.parse("daY"));
    }

    public void testParseWithDefault()
    {
        assertEquals(Unit.DAY, Unit.parse("daisy", Unit.DAY));
    }
}
