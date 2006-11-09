package com.zutubi.pulse.util;

import com.zutubi.pulse.test.PulseTestCase;

import java.util.Arrays;
import java.util.List;

/**
 */
public class StringUtilsTest extends PulseTestCase
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

    public void testWrapLineOnSpaceOnly()
    {
        assertEquals("1234567890", StringUtils.wrapString("1234567890", 5, null, false));
        assertEquals("12345678\n90", StringUtils.wrapString("12345678\n90", 5, null, false));
        assertEquals("12345678\n 90", StringUtils.wrapString("12345678 90", 5, null, false));
    }

    public void testGetLineLinefeed()
    {
        assertEquals("string", StringUtils.getLine("some\nstring\nhere", 2));
    }

    public void testGetLineCarriageReturn()
    {
        assertEquals("string", StringUtils.getLine("some\rstring\rhere", 2));
    }

    public void testGetLineCarriageReturnLinefeed()
    {
        assertEquals("string", StringUtils.getLine("some\r\nstring\r\nhere", 2));
    }

    public void testGetLineMixed()
    {
        assertEquals("w00t", StringUtils.getLine("using\r\ndifferent\nsplitters\rw00t\r\nto\nconfuse\r\nthings\n", 4));
    }

    public void testGetLineEmpty()
    {
        assertEquals("", StringUtils.getLine("third\nline\n\nis empty", 3));
    }

    public void testGetLinePastEnd()
    {
        assertNull(StringUtils.getLine("some\nlines\nhere", 4));
    }

    public void testSplitEmpty()
    {
        splitHelper("");
    }

    public void testSplitSpace()
    {
        splitHelper(" ");
    }

    public void testSplitSpaces()
    {
        splitHelper("   ");
    }

    public void testSplitSimple()
    {
        splitHelper("one two", "one", "two");
    }

    public void testSplitMore()
    {
        splitHelper("one two  three   four", "one", "two", "three", "four");
    }

    public void testSplitEscape()
    {
        splitHelper("one\\ two", "one two");
    }

    public void testSplitEscapeBackslash()
    {
        splitHelper("one\\\\ two", "one\\", "two");
    }

    public void testSplitQuotes()
    {
        splitHelper("hello \"you idiot\" there", "hello", "you idiot", "there");
    }

    public void testSplitEscapeQuote()
    {
        splitHelper("one\\\" two", "one\"", "two");
    }

    public void testSplitEscapeQuoteInQuotes()
    {
        splitHelper("\"one\\\"two\"", "one\"two");
    }

    public void testQuoteEmpty()
    {
        splitHelper("\"\"", "");
    }

    public void testQuoteEmptyAmongst()
    {
        splitHelper("wow \"\" empty", "wow", "", "empty");
    }

    public void testEndsInBackslash()
    {
        try
        {
            StringUtils.split("bad ending \\");
            fail();
        }
        catch(IllegalArgumentException e)
        {
            assertEquals("Unexpected end of input after backslash (\\)", e.getMessage());
        }
    }

    public void testUnfinishedQuotes()
    {
        try
        {
            StringUtils.split("\"bad ending");
            fail();
        }
        catch(IllegalArgumentException e)
        {
            assertEquals("Unexpected end of input looking for end of quote (\")", e.getMessage());
        }
    }

    public void testStringEquals()
    {
        assertTrue(StringUtils.equals(null, null));
        assertTrue(StringUtils.equals("", ""));
        assertTrue(StringUtils.equals("a", "a"));
        assertTrue(StringUtils.equals("abcde", "abcde"));

        assertFalse(StringUtils.equals("", null));
        assertFalse(StringUtils.equals("vvv", null));
        assertFalse(StringUtils.equals(null, ""));
        assertFalse(StringUtils.equals(null, "aa"));
        assertFalse(StringUtils.equals("aaa", "bbb"));
    }

    public void testJoin()
    {
        assertEquals("a,b,c", StringUtils.join(",", "a", "b", "c"));
    }

    public void testJoinGlueAllOver()
    {
        assertEquals(",a,,,b,,,c,", StringUtils.join(",", false, ",a,", ",b,", ",c,"));
    }

    public void testJoinGlueCheckNothingToDo()
    {
        assertEquals("a,b,c", StringUtils.join(",", true, "a", "b", "c"));
    }

    public void testJoinGlueCheckGlueAllOver()
    {
        assertEquals(",a,b,c,", StringUtils.join(",", true, ",a,", ",b,", ",c,"));
    }

    public void testJoinEmptyGlue()
    {
        assertEquals("abc", StringUtils.join("", true, "a", "b", "c"));    
    }

    public void testJoinEmptyPieces()
    {
        assertEquals(",,a,,b,,,c,", StringUtils.join(",", true, "", "", "a", "", "b", "", "", "c", ""));
    }

    public void testJoinGlueCheckGluePieces()
    {
        assertEquals(",a,,b,", StringUtils.join(",", true, ",", "a", ",", "b", ","));        
    }

    private void splitHelper(String s, String... expected)
    {
        List<String> expectedParts = Arrays.asList(expected);
        List<String> gotParts = StringUtils.split(s);
        assertEquals(expectedParts, gotParts);
    }
}
