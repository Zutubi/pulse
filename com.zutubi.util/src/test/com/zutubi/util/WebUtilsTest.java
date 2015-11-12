package com.zutubi.util;

import com.google.common.base.Predicates;
import com.zutubi.util.junit.ZutubiTestCase;

import java.io.UnsupportedEncodingException;
import java.util.Collection;

import static com.zutubi.util.WebUtils.buildQueryString;
import static com.zutubi.util.WebUtils.formUrlEncode;

public class WebUtilsTest extends ZutubiTestCase
{
    public void testFormUrlEncode()
    {
        // A simple sanity check, as the real work is done in Java APIs.
        assertEquals("space+here-slash%2Fthere", formUrlEncode("space here-slash/there"));
    }

    public void testBuildQueryStringEmpty()
    {
        assertEquals("", buildQueryString());
    }

    public void testBuildQueryStringSingleParam()
    {
        assertEquals("one=value", buildQueryString("one", "value"));
    }

    public void testBuildQueryStringMultipleParams()
    {
        assertEquals("one=1&two=2&three=3", buildQueryString("one", "1", "two", "2", "three", "3"));
    }

    public void testBuildQueryStringValuesEncoded()
    {
        assertEquals("a=fine&b=a+bit+%26%23%25%3D&c=ok", buildQueryString("a", "fine", "b", "a bit &#%=", "c", "ok"));
    }

    public void testEncodeAndJoinEmpty()
    {
        assertEquals("", WebUtils.encodeAndJoin(','));
    }

    public void testEncodeAndJoinSingle()
    {
        assertEquals("piece%2chere", WebUtils.encodeAndJoin(',', "piece,here"));
    }

    public void testEncodeAndJoinMultiple()
    {
        assertEquals("one,two%2cand,three%2c", WebUtils.encodeAndJoin(',', "one", "two,and", "three,"));
    }

    public void testEncodeAndJoinEmptyPiece()
    {
        assertEquals(",wow", WebUtils.encodeAndJoin(',', "", "wow"));
    }

    public void testSplitAndDecodeEmpty()
    {
        assertEquals(a(""), WebUtils.splitAndDecode(',', ""));
    }

    public void testSplitAndDecodeSingle()
    {
        assertEquals(a("hey,there"), WebUtils.splitAndDecode(',', "hey%2cthere"));
    }

    public void testSplitAndDecodeMultiple()
    {
        assertEquals(a("one", "two and", "three "), WebUtils.splitAndDecode(',', "one,two%20and,three%20"));
    }

    public void testSplitAndDecodeEmptyPiece()
    {
        assertEquals(a("", "wow"), WebUtils.splitAndDecode(',', ",wow"));
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
        String joined = WebUtils.encodeAndJoin(separator, pieces);
        Collection<String> split = WebUtils.splitAndDecode(separator, joined);
        assertEquals(pieces, split);
    }

    public void testURIPathDecodeEmpty()
    {
        assertEquals("", WebUtils.uriPathDecode(""));
    }

    public void testURIPathDecodeSlash()
    {
        assertEquals("/", WebUtils.uriPathDecode("/"));
    }

    public void testURIPathDecodeSlashes()
    {
        assertEquals("///", WebUtils.uriPathDecode("///"));
    }

    public void testURIPathDecodeVariousComponents()
    {
        assertEquals("/bits/and/sometimes//empty/pieces", WebUtils.uriPathDecode("/bits/and/sometimes//empty/pieces"));
    }

    public void testURIPathDecodeEndsWithSlash()
    {
        assertEquals("trailing/slash/", WebUtils.uriPathDecode("trailing/slash/"));
    }

    public void testURIPathDecodeEncodedSlashes()
    {
        assertEquals("after/decode/cannot/tell", WebUtils.uriPathDecode("after%2Fdecode/cannot%2Ftell"));
    }

    public void testURIPathDecodeComponentHasTrailingPercent()
    {
        assertEquals("path/%/here", WebUtils.uriPathDecode("path/%/here"));
    }

    public void testURIPathDecodeComponentWithIncompletePercent()
    {
        assertEquals("path/%e/here", WebUtils.uriPathDecode("path/%e/here"));
    }

    public void testURIPathDecodeComponentWithInvalidPercent()
    {
        assertEquals("path/%zz/here", WebUtils.uriPathDecode("path/%zz/here"));
    }

    public void testURIPathDecodePercent()
    {
        assertEquals("path/\u0001/here", WebUtils.uriPathDecode("path/%01/here"));
    }

    public void testURIPathDecodeMultiplePercents()
    {
        assertEquals("\u0001\u0002/path/\u0003\u0004\u0005/here", WebUtils.uriPathDecode("%01%02/path/%03%04%05/here"));
    }

    public void testURIPathDecodeComponentBeginsWithPercent()
    {
        assertEquals("path/\u0001abc/here", WebUtils.uriPathDecode("path/%01abc/here"));
    }

    public void testURIPathEncodeEmpty()
    {
        assertEquals("", WebUtils.uriPathEncode(""));
    }

    public void testURIPathEncode()
    {
        assertEquals("foo%20bar/baz%2bquux%3fquuux", WebUtils.uriPathEncode("foo bar/baz+quux?quuux"));
    }

    public void testURIPathEncodeAbsolute()
    {
        assertEquals("/absolute%20path", WebUtils.uriPathEncode("/absolute path"));
    }

    public void testURIPathEncodeDoubleSlash()
    {
        assertEquals("//absolute/path%2bhere", WebUtils.uriPathEncode("//absolute/path+here"));
    }

    public void testURIPathEncodeDoubleSlashMiddle()
    {
        assertEquals("double//slash/in%2bthe%20path", WebUtils.uriPathEncode("double//slash/in+the path"));
    }

    public void testURIComponentEncodeEmpty()
    {
        assertEquals("", WebUtils.uriComponentEncode(""));
    }

    public void testURIComponentEncodeNoChange()
    {
        assertEquals("agzALZ0369_-!~.()*", WebUtils.uriComponentEncode("agzALZ0369_-!~.()*"));
    }

    public void testURIComponentEncodeNonASCII() throws UnsupportedEncodingException
    {
        assertEquals("%e1%88%b4", WebUtils.uriComponentEncode("\u1234"));
    }

    public void testURIComponentEncodeForwardSlash()
    {
        assertEquals("%2f", WebUtils.uriComponentEncode("/"));
    }

    public void testURIComponentEncodeMixedFirstOK()
    {
        assertEquals("a%2f%20b%26c%23", WebUtils.uriComponentEncode("a/ b&c#"));
    }

    public void testURIComponentEncodeMixedFirstEncoded()
    {
        assertEquals("%3ahelp%2b%26m%24e", WebUtils.uriComponentEncode(":help+&m$e"));
    }

    public void testURIComponentDecodeEmpty()
    {
        assertEquals("", WebUtils.uriComponentDecode(""));
    }

    public void testURIComponentDecodeTrailingPercent()
    {
        assertEquals("%", WebUtils.uriComponentDecode("%"));
    }

    public void testURIComponentDecodeIncompletePercent()
    {
        assertEquals("%e", WebUtils.uriComponentDecode("%e"));
    }

    public void testURIComponentDecodeInvalidPercent()
    {
        assertEquals("%zz", WebUtils.uriComponentDecode("%zz"));
    }

    public void testURIComponentDecodePercent()
    {
        assertEquals("\u0001", WebUtils.uriComponentDecode("%01"));
    }

    public void testURIComponentDecodeMultiplePercents()
    {
        assertEquals("\u0001\u0002\u0003\u0004\u0005", WebUtils.uriComponentDecode("%01%02%03%04%05"));
    }

    public void testURIComponentDecodeBeginsWithPercent()
    {
        assertEquals("\u0001abc", WebUtils.uriComponentDecode("%01abc"));
    }

    public void testURIComponentDecodeEndsWithPercent()
    {
        assertEquals("abc\u0001", WebUtils.uriComponentDecode("abc%01"));
    }

    public void testURIComponentDecodeInvalidUTF8()
    {
        // FFFD is a replacement used when the incoming character is "unknown"
        assertEquals("\ufffd", WebUtils.uriComponentDecode("%e0"));
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
        assertEquals(s, WebUtils.uriComponentDecode(WebUtils.uriComponentEncode(s)));
    }

    public void testIsHtmlNameStartLower()
    {
        assertTrue(WebUtils.isHtmlNameStartChar('a'));
    }

    public void testIsHtmlNameStartUpper()
    {
        assertTrue(WebUtils.isHtmlNameStartChar('G'));
    }

    public void testIsHtmlNameStartDigit()
    {
        assertFalse(WebUtils.isHtmlNameStartChar('0'));
    }

    public void testIsHtmlNameStartUnderScore()
    {
        assertFalse(WebUtils.isHtmlNameStartChar('_'));
    }

    public void testIsHtmlNameStartRandom()
    {
        assertFalse(WebUtils.isHtmlNameStartChar('&'));
    }

    public void testIsHtmlNameLower()
    {
        assertTrue(WebUtils.isHtmlNameChar('a'));
    }

    public void testIsHtmlNameUpper()
    {
        assertTrue(WebUtils.isHtmlNameChar('G'));
    }

    public void testIsHtmlNameDigit()
    {
        assertTrue(WebUtils.isHtmlNameChar('0'));
    }

    public void testIsHtmlNameAllowedPunct()
    {
        assertTrue(WebUtils.isHtmlNameChar('_'));
        assertTrue(WebUtils.isHtmlNameChar('-'));
        assertTrue(WebUtils.isHtmlNameChar('.'));
        assertTrue(WebUtils.isHtmlNameChar(':'));
    }

    public void testIsHtmlNameRandom()
    {
        assertFalse(WebUtils.isHtmlNameChar('%'));
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

    public void testEncodeWithTag()
    {
        assertEquals("%21", WebUtils.encode('%', "!", Predicates.<Character>alwaysFalse()));
        assertEquals("%25", WebUtils.encode('%', "%", Predicates.<Character>alwaysFalse()));
        assertEquals("^", WebUtils.encode('%', "^", Predicates.<Character>alwaysTrue()));
    }

    public void testDecodeWithTag()
    {
        assertEquals("!", WebUtils.decode('%', "%21"));
        assertEquals("%", WebUtils.decode('%', "%25"));
        assertEquals("^", WebUtils.decode('%', "^"));
    }

    private void toValidHtmlNameHelper(String expected, String in)
    {
        assertEquals(expected, WebUtils.toValidHtmlName(in));
    }

    private String[] a(String... a)
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
}
