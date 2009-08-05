package com.zutubi.util;

import static com.zutubi.util.CollectionUtils.asPair;
import static com.zutubi.util.WebUtils.buildQueryString;
import static com.zutubi.util.WebUtils.formUrlEncode;
import com.zutubi.util.junit.ZutubiTestCase;

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
        assertEquals("one=value", buildQueryString(asPair("one", "value")));
    }

    public void testBuildQueryStringMultipleParams()
    {
        assertEquals("one=1&two=2&three=3", buildQueryString(asPair("one", "1"), asPair("two", "2"), asPair("three", "3")));
    }

    public void testBuildQueryStringValuesEncoded()
    {
        assertEquals("a=fine&b=a+bit+%26%23%25%3D&c=ok", buildQueryString(asPair("a", "fine"), asPair("b", "a bit &#%="), asPair("c", "ok")));
    }
}
