package com.zutubi.util;

import com.zutubi.util.junit.ZutubiTestCase;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class StringUtilsTest extends ZutubiTestCase
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

    public void testTrimStringNegativeLimit()
    {
        try
        {
            StringUtils.trimmedString("", -1);
            fail();
        }
        catch(IllegalArgumentException e)
        {
        }
    }

    public void testTrimStringCustomMessage()
    {
        assertEquals("this is a long s... [my message]", StringUtils.trimmedString("this is a long string that will need trimming", 32, "... [my message]"));
    }

    public void testTrimStringCustomMessageTooLong()
    {
        assertEquals("... [my me", StringUtils.trimmedString("this is a long string that will need trimming", 10, "... [my message]"));
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

    public void testUnsplitEmpty()
    {
        unsplitHelper("");
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

    public void testUnsplitSimple()
    {
        unsplitHelper("one two", "one", "two");
    }

    public void testSplitMore()
    {
        splitHelper("one two  three   four", "one", "two", "three", "four");
    }

    public void testUnsplitMore()
    {
        unsplitHelper("one two three four", "one", "two", "three", "four");
    }

    public void testSplitEscape()
    {
        splitHelper("one\\ two", "one two");
    }

    public void testSplitEscapeBackslash()
    {
        splitHelper("one\\\\ two", "one\\", "two");
    }

    public void testUnsplitEscapeBackslash()
    {
        unsplitHelper("one\\\\ two", "one\\", "two");
    }

    public void testSplitQuotes()
    {
        splitHelper("hello \"you idiot\" there", "hello", "you idiot", "there");
    }

    public void testUnsplitQuotes()
    {
        unsplitHelper("hello \"you idiot\" there", "hello", "you idiot", "there");
    }

    public void testSplitEscapeQuote()
    {
        splitHelper("one\\\" two", "one\"", "two");
    }

    public void testUnsplitEscapeQuote()
    {
        unsplitHelper("one\\\" two", "one\"", "two");
    }

    public void testSplitEscapeQuoteInQuotes()
    {
        splitHelper("\"one\\\"two\"", "one\"two");
    }

    public void testSplitQuoteInMiddle()
    {
        splitHelper("word\"quoted words\"another word", "wordquoted wordsanother", "word");
    }

    public void testSplitEmptyQuotesInMiddle()
    {
        splitHelper("word\"\"another word", "wordanother", "word");
    }

    public void testSplitQuoteEmpty()
    {
        splitHelper("\"\"", "");
    }

    public void testUnsplitQuoteEmpty()
    {
        unsplitHelper("\"\"", "");
    }

    public void testSplitQuoteEmptyAmongst()
    {
        splitHelper("wow \"\" empty", "wow", "", "empty");
    }

    public void testUnsplitQuoteEmptyAmongst()
    {
        unsplitHelper("wow \"\" empty", "wow", "", "empty");
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

    public void testJoinSkipEmpty()
    {
        assertEquals("a,b,c", StringUtils.join(",", true, true, "", "", "a", "", "b", "", "", "c", ""));
    }

    public void testUrlEncodePath()
    {
        assertEquals("foo%20bar/baz+quux%3Fquuux", StringUtils.urlEncodePath("foo bar/baz+quux?quuux"));
    }
    
    public void testUrlEncodePathAbsolute()
    {
        assertEquals("/absolute%20path", StringUtils.urlEncodePath("/absolute path"));
    }

    public void testUrlEncodePathDoubleSlash()
    {
        assertEquals("//absolute/path+here", StringUtils.urlEncodePath("//absolute/path+here"));
    }

    public void testUrlEncodePathDoubleSlashMiddle()
    {
        assertEquals("double//slash/in+the%20path", StringUtils.urlEncodePath("double//slash/in+the path"));
    }

    public void testUrlEncodePathEmpty()
    {
        assertEquals("", StringUtils.urlEncodePath(""));
    }

    public void testEncodeAndJoinEmpty()
    {
        assertEquals("", StringUtils.encodeAndJoin(','));
    }

    public void testEncodeAndJoinSingle()
    {
        assertEquals("piece%2chere", StringUtils.encodeAndJoin(',', "piece,here"));
    }

    public void testEncodeAndJoinMultiple()
    {
        assertEquals("one,two%2cand,three%2c", StringUtils.encodeAndJoin(',', "one", "two,and", "three,"));
    }

    public void testEncodeAndJoinEmptyPiece()
    {
        assertEquals(",wow", StringUtils.encodeAndJoin(',', "", "wow"));
    }

    public void testSplitAndDecodeEmpty()
    {
        assertEquals(a(""), StringUtils.splitAndDecode(',', ""));
    }

    public void testSplitAndDecodeSingle()
    {
        assertEquals(a("hey,there"), StringUtils.splitAndDecode(',', "hey%2cthere"));
    }

    public void testSplitAndDecodeMultiple()
    {
        assertEquals(a("one", "two and", "three "), StringUtils.splitAndDecode(',', "one,two%20and,three%20"));
    }

    public void testSplitAndDecodeEmptyPiece()
    {
        assertEquals(a("", "wow"), StringUtils.splitAndDecode(',', ",wow"));
    }

    public void testShortHandJoinEncoded()
    {
        joinEncodedHelper(',', "hello", "there,sailor");
    }

    public void testShortHandJoinEncodedPercent()
    {
        joinEncodedHelper(',', "hello", "there%2csailor");
    }

    private void joinEncodedHelper(char separator, String... pieces)
    {
        String joined = StringUtils.encodeAndJoin(separator, pieces);
        Collection<String> split = StringUtils.splitAndDecode(separator, joined);
        assertEquals(pieces, split);
    }

    public void testURIComponentEncodeEmpty()
    {
        assertEquals("", StringUtils.uriComponentEncode(""));
    }

    public void testURIComponentEncodeNoChange()
    {
        assertEquals("agzALZ0369_-!~.'()*", StringUtils.uriComponentEncode("agzALZ0369_-!~.'()*"));
    }

    public void testURIComponentEncodeNonASCII() throws UnsupportedEncodingException
    {
        assertEquals("%e1%88%b4", StringUtils.uriComponentEncode("\u1234"));
    }

    public void testURIComponentEncodeForwardSlash()
    {
        assertEquals("%2f", StringUtils.uriComponentEncode("/"));
    }

    public void testURIComponentEncodeMixedFirstOK()
    {
        assertEquals("a%2f%20b%26c%23", StringUtils.uriComponentEncode("a/ b&c#"));
    }

    public void testURIComponentEncodeMixedFirstEncoded()
    {
        assertEquals("%3ahelp%2b%26m%24e", StringUtils.uriComponentEncode(":help+&m$e"));
    }

    public void testURIComponentDecodeEmpty()
    {
        assertEquals("", StringUtils.uriComponentDecode(""));
    }

    public void testURIComponentDecodeTrailingPercent()
    {
        assertEquals("%", StringUtils.uriComponentDecode("%"));
    }

    public void testURIComponentDecodeIncompletePercent()
    {
        assertEquals("%e", StringUtils.uriComponentDecode("%e"));
    }

    public void testURIComponentDecodeInvalidPercent()
    {
        assertEquals("%zz", StringUtils.uriComponentDecode("%zz"));
    }

    public void testURIComponentDecodePercent()
    {
        assertEquals("\u0001", StringUtils.uriComponentDecode("%01"));
    }

    public void testURIComponentDecodeMultiplePercents()
    {
        assertEquals("\u0001\u0002\u0003\u0004\u0005", StringUtils.uriComponentDecode("%01%02%03%04%05"));
    }

    public void testURIComponentDecodeBeginsWithPercent()
    {
        assertEquals("\u0001abc", StringUtils.uriComponentDecode("%01abc"));
    }

    public void testURIComponentDecodeEndsWithPercent()
    {
        assertEquals("abc\u0001", StringUtils.uriComponentDecode("abc%01"));
    }

    public void testURIComponentDecodeInvalidUTF8()
    {
        // FFFD is a replacement used when the incoming character is "unknown"
        assertEquals("\ufffd", StringUtils.uriComponentDecode("%e0"));
    }

    public void testURIComponentRoundTripNonASCII()
    {
        uriComponentHelper("\u1234");
    }

    public void testURIComponentRoundTripNoChange()
    {
        uriComponentHelper("agzALZ0369_-!~.'()*");
    }

    public void testURIComponentRoundTripSomeSpecials()
    {
        uriComponentHelper("a/b%c@@f%%");
    }

    private void uriComponentHelper(String s)
    {
        assertEquals(s, StringUtils.uriComponentDecode(StringUtils.uriComponentEncode(s)));
    }
    private void splitHelper(String s, String... expected)
    {
        List<String> expectedParts = Arrays.asList(expected);
        List<String> gotParts = StringUtils.split(s);
        assertEquals(expectedParts, gotParts);
    }

    private void unsplitHelper(String expected, String... pieces)
    {
        String got = StringUtils.unsplit(Arrays.asList(pieces));
        assertEquals(expected, got);
    }

    public void testGetNextTokenEmpty()
    {
        getNextTokenHelper("", false, null, null);
    }

    public void testGetNextTokenEmptySkipEmpty()
    {
        getNextTokenHelper("", true, null, null);
    }

    public void testGetNextTokenSeparator()
    {
        getNextTokenHelper("/", false, "", "");
    }

    public void testGetNextTokenSeparatorSkipEmpty()
    {
        getNextTokenHelper("/", true, null, null);
    }

    public void testGetNextTokenTwoSeparators()
    {
        getNextTokenHelper("//", false, "", "/");
    }

    public void testGetNextTokenTwoSeparatorsSkipEmpty()
    {
        getNextTokenHelper("//", true, null, null);
    }

    public void testGetNextTokenStartsWithSeparator()
    {
        getNextTokenHelper("/a", false, "", "a");
    }

    public void testGetNextTokenStartsWithSeparatorSkipEmpty()
    {
        getNextTokenHelper("/a", true, "a", "");
    }

    public void testGetNextTokenStartsWithTwoSeparators()
    {
        getNextTokenHelper("//a", false, "", "/a");
    }

    public void testGetNextTokenStartsWithTwoSeparatorsSkipEmpty()
    {
        getNextTokenHelper("//a", true, "a", "");
    }

    public void testGetNextTokenEndsWithSeparator()
    {
        getNextTokenHelper("a/", false, "a", "");
    }

    public void testGetNextTokenEndsWithSeparatorSkipEmpty()
    {
        getNextTokenHelper("a/", true, "a", "");
    }

    public void testGetNextTokenSingleSeparator()
    {
        getNextTokenHelper("a/b", false, "a", "b");
    }

    public void testGetNextTokenSingleSeparatorSkipEmpty()
    {
        getNextTokenHelper("a/b", true, "a", "b");
    }

    public void testGetNextTokenEmptyElement()
    {
        getNextTokenHelper("a//b", false, "a", "/b");
    }

    public void testGetNextTokenEmptyElementSkipEmpty()
    {
        getNextTokenHelper("a//b", true, "a", "/b");
    }

    private void getNextTokenHelper(String s, boolean skipEmpty, String expectedToken, String expectedRemainder)
    {
        String[] result = StringUtils.getNextToken(s, '/', skipEmpty);
        if(expectedToken == null)
        {
            assertNull(result);
        }
        else
        {
            assertNotNull(result);
            assertEquals(2, result.length);
            assertEquals(expectedToken, result[0]);
            assertEquals(expectedRemainder, result[1]);
        }
    }

    public void testIsAsciiLowerCaseLowBoundary()
    {
        assertTrue(StringUtils.isAsciiLowerCase('a'));
    }

    public void testIsAsciiLowerCaseMiddle()
    {
        assertTrue(StringUtils.isAsciiLowerCase('j'));
    }

    public void testIsAsciiLowerCaseHighBoundary()
    {
        assertTrue(StringUtils.isAsciiLowerCase('z'));
    }

    public void testIsAsciiLowerCasePunct()
    {
        assertFalse(StringUtils.isAsciiLowerCase(' '));
    }

    public void testIsAsciiLowerCaseUpper()
    {
        assertFalse(StringUtils.isAsciiLowerCase('A'));
    }

    public void testIsAsciiLowerCaseBelowLow()
    {
        assertFalse(StringUtils.isAsciiLowerCase('`'));
    }

    public void testIsAsciiLowerCaseAboveHigh()
    {
        assertFalse(StringUtils.isAsciiLowerCase('{'));
    }

    public void testIsAsciiUpperCaseLowBoundary()
    {
        assertTrue(StringUtils.isAsciiUpperCase('A'));
    }

    public void testIsAsciiUpperCaseMiddle()
    {
        assertTrue(StringUtils.isAsciiUpperCase('M'));
    }

    public void testIsAsciiUpperCaseHighBoundary()
    {
        assertTrue(StringUtils.isAsciiUpperCase('Z'));
    }

    public void testIsAsciiUpperCasePunct()
    {
        assertFalse(StringUtils.isAsciiUpperCase('.'));
    }

    public void testIsAsciiUpperCaseLower()
    {
        assertFalse(StringUtils.isAsciiUpperCase('d'));
    }

    public void testIsAsciiUpperCaseBelowLow()
    {
        assertFalse(StringUtils.isAsciiUpperCase('@'));
    }
    
    public void testIsAsciiUpperCaseAboveHigh()
    {
        assertFalse(StringUtils.isAsciiUpperCase('['));
    }

    public void testIsAsciiDigitLowBoundary()
    {
        assertTrue(StringUtils.isAsciiDigit('0'));
    }

    public void testIsAsciiDigitMiddle()
    {
        assertTrue(StringUtils.isAsciiDigit('2'));
    }

    public void testIsAsciiDigitHighBoundary()
    {
        assertTrue(StringUtils.isAsciiDigit('9'));
    }

    public void testIsAsciiDigitRandom()
    {
        assertFalse(StringUtils.isAsciiDigit('j'));
    }

    public void testIsAsciiDigitPunct()
    {
        assertFalse(StringUtils.isAsciiDigit('!'));
    }

    public void testIsAsciiDigitBelowLow()
    {
        assertFalse(StringUtils.isAsciiDigit('/'));
    }

    public void testIsAsciiDigitAboveHigh()
    {
        assertFalse(StringUtils.isAsciiDigit(':'));
    }

    public void testIsAsciiAlphabeticalUpper()
    {
        assertTrue(StringUtils.isAsciiAlphabetical('A'));
    }

    public void testIsAsciiAlphabeticalLower()
    {
        assertTrue(StringUtils.isAsciiAlphabetical('z'));
    }

    public void testIsAsciiAlphabeticalBetween()
    {
        assertFalse(StringUtils.isAsciiAlphabetical('^'));
    }

    public void testIsAsciiAlphabeticalRandom()
    {
        assertFalse(StringUtils.isAsciiAlphabetical(' '));
    }

    public void testIsAsciiAlphaNumericUpper()
    {
        assertTrue(StringUtils.isAsciiAlphaNumeric('A'));
    }

    public void testIsAsciiAlphaNumericLower()
    {
        assertTrue(StringUtils.isAsciiAlphaNumeric('z'));
    }

    public void testIsAsciiAlphaNumericDigit()
    {
        assertTrue(StringUtils.isAsciiAlphaNumeric('0'));
    }

    public void testIsAsciiAlphaNumericBetween()
    {
        assertFalse(StringUtils.isAsciiAlphaNumeric('='));
    }

    public void testIsAsciiAlphaNumericRandom()
    {
        assertFalse(StringUtils.isAsciiAlphabetical('~'));
    }

    public void testIsHtmlNameStartLower()
    {
        assertTrue(StringUtils.isHtmlNameStartChar('a'));
    }

    public void testIsHtmlNameStartUpper()
    {
        assertTrue(StringUtils.isHtmlNameStartChar('G'));
    }

    public void testIsHtmlNameStartDigit()
    {
        assertFalse(StringUtils.isHtmlNameStartChar('0'));
    }

    public void testIsHtmlNameStartUnderScore()
    {
        assertFalse(StringUtils.isHtmlNameStartChar('_'));
    }

    public void testIsHtmlNameStartRandom()
    {
        assertFalse(StringUtils.isHtmlNameStartChar('&'));
    }

    public void testIsHtmlNameLower()
    {
        assertTrue(StringUtils.isHtmlNameChar('a'));
    }

    public void testIsHtmlNameUpper()
    {
        assertTrue(StringUtils.isHtmlNameChar('G'));
    }

    public void testIsHtmlNameDigit()
    {
        assertTrue(StringUtils.isHtmlNameChar('0'));
    }

    public void testIsHtmlNameAllowedPunct()
    {
        assertTrue(StringUtils.isHtmlNameChar('_'));
        assertTrue(StringUtils.isHtmlNameChar('-'));
        assertTrue(StringUtils.isHtmlNameChar('.'));
        assertTrue(StringUtils.isHtmlNameChar(':'));
    }

    public void testIsHtmlNameRandom()
    {
        assertFalse(StringUtils.isHtmlNameChar('%'));
    }

    public void testToValidHtmlNameEmpty()
    {
        toValidHtmlNameHelper("a", "");
    }

    public void testToValidHtmlNameDigit()
    {
        toValidHtmlNameHelper("a0", "0");
    }

    public void testToValidHtmlNameInvalid()
    {
        toValidHtmlNameHelper("a.", "/");
    }

    public void testToValidHtmlNameLetter()
    {
        toValidHtmlNameHelper("b", "b");
    }

    public void testToValidHtmlNameValid()
    {
        toValidHtmlNameHelper("valid-id:here_0", "valid-id:here_0");
    }

    public void testToValidHtmlNameAllInvalid()
    {
        toValidHtmlNameHelper("a.....", "!@#$%");
    }

    public void testToValidHtmlNamePath()
    {
        toValidHtmlNameHelper("foo.bar.baz", "foo/bar/baz");
    }

    private void toValidHtmlNameHelper(String expected, String in)
    {
        assertEquals(expected, StringUtils.toValidHtmlName(in));
    }

    public void testStripLineBreaksEmpty()
    {
        assertEquals("", StringUtils.stripLineBreaks(""));
    }

    public void testStripLineBreaksNoBreaks()
    {
        assertEquals("no line breaks!", StringUtils.stripLineBreaks("no line breaks!"));
    }

    public void testStripLineBreaksLineFeed()
    {
        assertEquals("", StringUtils.stripLineBreaks("\n"));
    }

    public void testStripLineBreaksCarriageReturn()
    {
        assertEquals("", StringUtils.stripLineBreaks("\r"));
    }

    public void testStripLineBreaksCarriageReturnLineFeed()
    {
        assertEquals("", StringUtils.stripLineBreaks("\r\n"));
    }

    public void testStripLineBreaksAmongstText()
    {
        assertEquals("afewlineswithvarious breaksin between", StringUtils.stripLineBreaks("a\nfew\rlines\r\nwith\n\nvarious breaks\n\rin between\r\r"));
    }

    public void testSplitChEmpty()
    {
        splitChHelper("");
    }

    public void testSplitChSeparator()
    {
        splitChHelper("/");
    }

    public void testSplitChMultiSeparator()
    {
        splitChHelper("///");
    }

    public void testSplitChSingleCharacter()
    {
        splitChHelper("a");
    }

    public void testSplitChMultiCharacter()
    {
        splitChHelper("abc");
    }

    public void testSplitChMatchStart()
    {
        splitChHelper("/abc");
    }

    public void testSplitChMatchMiddle()
    {
        splitChHelper("a/bc");
    }

    public void testSplitChMatchEnd()
    {
        splitChHelper("abc/");
    }

    public void testSplitChMultipleMatches()
    {
        splitChHelper("a/b/c");
    }

    public void testSplitChMultipleMatchesIncludingStartAndEnd()
    {
        splitChHelper("/abc");
    }

    public void testSplitChAdjacentSeparators()
    {
        splitChHelper("a//bc");
    }

    public void testSplitChAdjacentSeparatorsStart()
    {
        splitChHelper("//abc");
    }

    public void testSplitChAdjacentSeparatorsEnd()
    {
        splitChHelper("abc//");
    }

    private void splitChHelper(String s)
    {
        String[] javaApi = s.split("/");
        assertEquals(javaApi, StringUtils.split(s, '/'));

        javaApi = CollectionUtils.filterToArray(javaApi, new Predicate<String>()
        {
            public boolean satisfied(String s)
            {
                return s.length() > 0;
            }
        });

        assertEquals(javaApi, StringUtils.split(s, '/', true));
    }

    public void testTimesEmptyString()
    {
        assertEquals("", StringUtils.times("", 10));
    }

    public void testTimesZeroCount()
    {
        assertEquals("", StringUtils.times("hello", 0));
    }

    public void testTimesOneCount()
    {
        assertEquals("hello", StringUtils.times("hello", 1));
    }

    public void testTimes()
    {
        assertEquals("123123123", StringUtils.times("123", 3));
    }

    public void testStripLeadingWhitespace()
    {
        assertEquals("", StringUtils.stripLeadingWhitespace(""));
        assertEquals("", StringUtils.stripLeadingWhitespace(" "));
        assertEquals("", StringUtils.stripLeadingWhitespace("  "));
        assertEquals("abc", StringUtils.stripLeadingWhitespace("abc"));
        assertEquals("abc", StringUtils.stripLeadingWhitespace(" abc"));
        assertEquals("abc", StringUtils.stripLeadingWhitespace("  abc"));
        assertEquals("abc  ", StringUtils.stripLeadingWhitespace("  abc  "));
        assertEquals("", StringUtils.stripLeadingWhitespace("  \n   "));
    }

    public void testStripPrefixEmptyEmpty()
    {
        assertEquals("", StringUtils.stripPrefix("", ""));
    }

    public void testStripPrefixEmptyNonEmpty()
    {
        assertEquals("", StringUtils.stripPrefix("", "non"));
    }

    public void testStripPrefixNonEmptyEmpty()
    {
        assertEquals("non", StringUtils.stripPrefix("non", ""));
    }

    public void testStripPrefixNonMatching()
    {
        assertEquals("nomatch", StringUtils.stripPrefix("nomatch", "non"));
    }

    public void testStripPrefixMatching()
    {
        assertEquals("atch", StringUtils.stripPrefix("nomatch", "nom"));
    }

    public void testCountEmpty()
    {
        assertEquals(0, StringUtils.count("", 'x'));
    }

    public void testCountNoOccurrences()
    {
        assertEquals(0, StringUtils.count("there are no ecses", 'x'));
    }

    public void testCountOneOccurrence()
    {
        assertEquals(1, StringUtils.count("there is one x here", 'x'));
    }
    
    public void testCountMultipleOccurrences()
    {
        assertEquals(5, StringUtils.count("how do you spell xxxx? one x or two?", 'x'));
    }

    private <T> T[] a(T... a)
    {
        return a;
    }

    private void assertEquals(String[] expected, Collection<String> got)
    {
        assertEquals(expected.length, got.size());
        int i = 0;
        for(String s: got)
        {
            assertEquals(expected[i++], s);
        }
    }

    private void assertEquals(String[] expected, String[] got)
    {
        assertTrue("Expected " + format(expected) + ", got " + format(got), Arrays.equals(expected, got));
    }

    private String format(String[] a)
    {
        StringBuilder result = new StringBuilder();
        result.append('[');
        boolean first = true;
        for(String s: a)
        {
            if(first)
            {
                first = false;
            }
            else
            {
                result.append(", ");
            }
            result.append('"');
            result.append(s);
            result.append('"');
        }

        result.append(']');
        return result.toString();
    }
}