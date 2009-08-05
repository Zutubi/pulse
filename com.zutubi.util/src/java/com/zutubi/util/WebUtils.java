package com.zutubi.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Miscellaneous utility methods useful for web-related things like URLs, HTML,
 * HTTP, etc.
 */
public class WebUtils
{
    /**
     * Encodes the given string in application/x-www-form-urlencoded format.
     * Note that this is *not* the same as encoding the string to be part of
     * a URL itself.  It is suitable for POST data and query strings.
     *
     * @param s the string to encode
     * @return application/x-www-form-urlencoded form of the string
     * @see URLEncoder
     */
    public static String formUrlEncode(String s)
    {
        try
        {
            return URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            // Should never happen (UTF-8 always available).  If it does, best
            // to fail early.
            throw new RuntimeException(e);
        }
    }

    /**
     * Build a query string from the given pairs of parameters and values.  The
     * parameter names are included verbatim, the values encoded.  The returned
     * string may be appended to a URL following a ?.
     *
     * @param params name-value pairs for parameters to build the query string
     *               out of
     * @return an encoded query string, ready to append after a question mark
     */
    public static String buildQueryString(Pair<String, String>... params)
    {
        StringBuilder sb = new StringBuilder();
        for (Pair<String, String> param: params)
        {
            if (sb.length() > 0)
            {
                sb.append('&');
            }

            sb.append(param.first);
            sb.append('=');
            sb.append(formUrlEncode(param.second));
        }

        return sb.toString();
    }
}
