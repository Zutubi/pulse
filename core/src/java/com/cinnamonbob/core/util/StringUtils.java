package com.cinnamonbob.core.util;

/**
 */
public class StringUtils
{
    /**
     * Returns the given string, trimmed if necessary to the given maximum
     * length.  Upon trimming, the last 3 characters in the returned string
     * will be "...": and the returned string will be exactly length
     * characters long including these dots.  If 3 characters cannot fit
     * under the limit, only length dots will be returned.
     *
     * @param s      the string to trim
     * @param length the maximum length of the returned string
     * @return the given string, trimmed if necessary
     */
    public static String trimmedString(String s, int length)
    {
        if (s.length() > length)
        {
            if (length >= 3)
            {
                return s.substring(0, length - 3) + "...";
            }
            else
            {
                String result = "";
                for (int i = 0; i < length; i++)
                {
                    result += ".";
                }

                return result;
            }
        }

        return s;
    }
}
