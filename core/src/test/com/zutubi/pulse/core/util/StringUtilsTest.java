package com.zutubi.pulse.core.util;

import com.zutubi.pulse.test.BobTestCase;

/**
 */
public class StringUtilsTest extends BobTestCase
{
    public void testTrimStringShort()
    {
        assertEquals("12345", StringUtils.trimmedString("12345", 10));
    }

    public void testTrimStringMuchLonger()
    {
        assertEquals("12...", StringUtils.trimmedString("1234567890", 5));
    }

    public void testTrimStringExact()
    {
        assertEquals("12345", StringUtils.trimmedString("12345", 5));
    }

    public void testTrimStringJustOver()
    {
        assertEquals("12...", StringUtils.trimmedString("123456", 5));
    }

    public void testTrimStringShortLimit()
    {
        assertEquals("..", StringUtils.trimmedString("12345", 2));
    }

    public void testTrimStringDotsLimit()
    {
        assertEquals("...", StringUtils.trimmedString("12345", 3));
    }

    public void testTrimStringZeroLimit()
    {
        assertEquals("", StringUtils.trimmedString("12345", 0));
    }

    public void testWrapShort()
    {
        assertEquals("12345", StringUtils.wrapString("12345", 10, null));
    }

    public void testWrapSimple()
    {
        assertEquals("12345\n67890", StringUtils.wrapString("12345 67890", 5, null));
    }

    public void testWrapEarlierSpace()
    {
        assertEquals("123\n4567", StringUtils.wrapString("123 4567", 5, null));
    }

    public void testWrapMultiline()
    {
        assertEquals("this is a\nvery fine\nmultiline\nexample", StringUtils.wrapString("this is a very fine multiline example", 9, null));
    }

    public void testWrapNoSpace()
    {
        assertEquals("12345\n67890", StringUtils.wrapString("1234567890", 5, null));
    }

    public void testWrapPrefix()
    {
        assertEquals("12345\n=6789\n=0", StringUtils.wrapString("1234567890", 5, "="));
    }

    public void testWrapSomeText()
    {
        assertEquals("  * this is a sample of the\n" +
                "    sorts of wacky things that\n" +
                "    we might need the wrapping\n" +
                "    function to have a go at,\n" +
                "    including the possibility\n" +
                "    of long\n" +
                "    striiiiiiiiiiiiiiiiiiiiiii\n" +
                "    iiiiiiiiiiiiiiiiiiiiiiiiii\n" +
                "    iiiiiiiiiiiiiiiiiiiiiiiiii\n" +
                "    iiiiiiiiiiiiiiiiiiiiiiiiii\n" +
                "    iiiiiiiiiiiiiiiiiiiiiiiiii\n" +
                "    iiiiiiiiiiiiiiiiiiiiiiiiii\n" +
                "    iiiiiiiiiiiiiiiiiiiiiiiiin\n" +
                "    gs of random junk to throw\n" +
                "    things right out of wack",
                StringUtils.wrapString("  * this is a sample of the sorts of wacky things " +
                        "that we might need the wrapping function to have a go " +
                        "at, including the possibility of long striiiiiiiiiiiiii" +
                        "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii" +
                        "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii" +
                        "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiin" +
                        "gs of random junk to throw things right out of wack",
                        30, "    "));
    }

    public void testInvalidPrefix()
    {
        try
        {
            StringUtils.wrapString("", 3, "pr");
            fail();
        }
        catch (IllegalArgumentException e)
        {

        }
    }
}
