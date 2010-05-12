package com.zutubi.pulse.master.committransformers;

import java.util.regex.Matcher;

/**
 * Static utilities for helping with regex substitutions.
 */
public class SubstitutionUtils
{
    /**
     * The following code was taken from Matcher.appendReplacement.  It is
     * housed here so as not to encourage wide use: the Java Regex APIs are
     * preferable.
     *
     * @param replacement
     * @param matcher
     * @return
     * @see java.util.regex.Matcher#appendReplacement(StringBuffer,String)
     */
    public static String processSubstitution(String replacement, Matcher matcher)
    {
        // Process substitution string to replace group references with groups
        int cursor = 0;

        StringBuilder result = new StringBuilder();

        while (cursor < replacement.length())
        {
            char nextChar = replacement.charAt(cursor);
            if (nextChar == '\\')
            {
                cursor++;
                nextChar = replacement.charAt(cursor);
                result.append(nextChar);
                cursor++;
            }
            else if (nextChar == '$')
            {
                // Skip past $
                cursor++;

                // The first number is always a group
                int refNum = (int) replacement.charAt(cursor) - '0';
                if ((refNum < 0) || (refNum > 9))
                {
                    throw new IllegalArgumentException("Illegal group reference");
                }

                cursor++;

                // Capture the largest legal group string
                boolean done = false;
                while (!done)
                {
                    if (cursor >= replacement.length())
                    {
                        break;
                    }
                    int nextDigit = replacement.charAt(cursor) - '0';
                    if ((nextDigit < 0) || (nextDigit > 9))
                    { // not a number
                        break;
                    }
                    int newRefNum = (refNum * 10) + nextDigit;
                    if (matcher.groupCount() < newRefNum)
                    {
                        done = true;
                    }
                    else
                    {
                        refNum = newRefNum;
                        cursor++;
                    }
                }

                // Append group
                if (matcher.group(refNum) != null)
                {
                    result.append(matcher.group(refNum));
                }
            }
            else
            {
                result.append(nextChar);
                cursor++;
            }
        }
        return result.toString();
    }
}
