package com.zutubi.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class StringUtils
{
    /**
     * A null safe equals method for strings.
     *
     * @param a first string
     * @param b second string
     * @return true if the strings are equal, false otherwise.
     */
    public static boolean equals(String a, String b)
    {
        if (a == null)
        {
            return b == null;
        }
        else
        {
            if (b == null)
            {
                return false;
            }
        }
        return a.equals(b);
    }

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

    public static String wrapString(String s, int lineLength, String prefix)
    {
        return wrapString(s, lineLength, prefix, true);
    }

    /**
     * Returns a version of the given string wrapped if necessary to ensure
     * it does not exceed the line length satisfied.  The string will be
     * wrapped at whitespace if possible but if not will be broken whereever
     * necessary to ensure the line length is not exceeded.  Wrapping is done
     * by replacing a space with a newline, or inserting a newline (when no
     * space is found to replace).  Existing newlines are ignored.
     *
     * @param s          the string to wrap
     * @param lineLength the maximum length of lines returned in the result
     * @param prefix     An optional prefix to add after each inserted newline
     *                   character.  The returned lines will still not exceed
     *                   lineLength.  Thus the prefix length must be less than
     *                   lineLength - 1.  If null is passed, no prefix is used.
     * @param splitWord  if set to true, this method will introduce a newline
     *                   in the middle of a word to ensure the the lineLength is
     *                   not exceeded. If false, this method will search for the
     *                   nearest appropriate whitespace at which to split.
     * @return a version of the given string with newlines inserted to
     *         wrap lines at the given length
     */
    public static String wrapString(String s, int lineLength, String prefix, boolean splitWord)
    {
        if (prefix != null && prefix.length() >= lineLength - 1)
        {
            throw new IllegalArgumentException("prefix length must be less than line length -1");
        }

        // Short circuit a common case
        if (s.length() < lineLength)
        {
            return s;
        }

        int length = s.length();
        int effectiveLineLength = lineLength;
        StringBuilder result = new StringBuilder(length + length * 2 / lineLength);

        for (int i = 0; i < length;)
        {
            if (length - i <= effectiveLineLength)
            {
                // Last bit
                result.append(s.substring(i));
                break;
            }

            // Check for existing newlines in this span
            int j;
            boolean alreadySplit = false;
            for (j = i + effectiveLineLength; j >= i; j--)
            {
                if (s.charAt(j) == '\n')
                {
                    // Already split at this point, continue from the split
                    alreadySplit = true;
                    result.append(s.substring(i, j + 1));
                    if (prefix != null)
                    {
                        result.append(prefix);
                    }
                    i = j + 1;
                    break;
                }
            }

            if (!alreadySplit)
            {
                // Need to find a place to trim, starting at i + effectiveLineLength
                int candidate = i + effectiveLineLength;
                for (j = candidate; j > i; j--)
                {
                    if (s.charAt(j) == ' ')
                    {
                        // OK, found a spot to split
                        result.append(s.substring(i, j));
                        result.append('\n');
                        if (prefix != null)
                        {
                            result.append(prefix);
                        }

                        i = j + 1;
                        break;
                    }
                }

                if (j == i)
                {
                    // No space found.
                    if (splitWord)
                    {
                        result.append(s.substring(i, candidate));
                        result.append('\n');
                        if (prefix != null)
                        {
                            result.append(prefix);
                        }
                        i = candidate;
                    }
                    else
                    {
                        // find the next space and split on it.
                        for (int k = candidate; k < s.length(); k++)
                        {
                            if (Character.isWhitespace(s.charAt(k)))
                            {
                                // good point to split.
                                result.append(s.substring(i, k));
                                result.append('\n');
                                if (s.charAt(k) == '\n')
                                {
                                    // dont need a second new line.
                                    k = k + 1;
                                }

                                if (prefix != null)
                                {
                                    result.append(prefix);
                                }

                                i = k;
                                break;
                            }
                        }
                        if (j == i)
                        {
                            // no whitespace located.
                            result.append(s.substring(i));
                            break;
                        }
                    }
                }
            }

            if (prefix != null)
            {
                effectiveLineLength = lineLength - prefix.length();
            }
        }

        return result.toString();
    }

    /**
     * Returns the line'th line in the given string, where lines are
     * separated by any one of \r, \n or \r\n.
     *
     * @param s    the string to extract the line from
     * @param line the one-based number of the line to extract
     * @return the given line, or null if there are not that many lines
     */
    public static String getLine(String s, int line)
    {
        String[] lines = s.split("\r\n|\n|\r");
        if (lines.length >= line)
        {
            return lines[line - 1];
        }
        else
        {
            return null;
        }
    }

    public static int getLineOffset(String s, int line)
    {
        s = s.replace("\r\n", "\n");
        s = s.replace("\r", "\n");

        int currentLine = 1;
        for (int i = 0; i < s.length(); i++)
        {
            if (currentLine == line)
            {
                return i;
            }

            if (s.charAt(i) == '\r')
            {
                currentLine++;
                if (i + 1 < s.length() && s.charAt(i + 1) == '\n')
                {
                    i++;
                }
            }
            else if (s.charAt(i) == '\n')
            {
                currentLine++;
            }
        }

        return -1;
    }

    /**
     * Splits the given string at spaces, allowing use of quoting to override
     * spaces (i.e. foo bar is split into [foo, bar] but "foo bar" gives
     * [foo bar]).  Backslashes may be used to escape quotes or spaces.
     *
     * @param s the string to split
     * @return a list containing the split parts of the string
     * @throws IllegalArgumentException if the string is poorly formatted
     */
    public static List<String> split(String s)
    {
        List<String> result = new LinkedList<String>();
        boolean inQuotes = false;
        boolean escaped = false;
        boolean haveData = false;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            if (escaped)
            {
                haveData = true;
                current.append(c);
                escaped = false;
            }
            else
            {
                switch (c)
                {
                    case'\\':
                    {
                        escaped = true;
                        break;
                    }
                    case' ':
                    {
                        if (inQuotes)
                        {
                            current.append(c);
                        }
                        else if (haveData)
                        {
                            result.add(current.toString());
                            current.delete(0, current.length());
                            haveData = false;
                        }

                        break;
                    }
                    case'"':
                    {
                        if (inQuotes)
                        {
                            inQuotes = false;
                        }
                        else
                        {
                            inQuotes = true;
                            // We always have data if we see quotes, which
                            // allows expression of the empty string as ""
                            haveData = true;
                        }

                        break;
                    }
                    default:
                    {
                        current.append(c);
                        haveData = true;
                    }
                }
            }
        }

        if (escaped)
        {
            throw new IllegalArgumentException("Unexpected end of input after backslash (\\)");
        }
        if (inQuotes)
        {
            throw new IllegalArgumentException("Unexpected end of input looking for end of quote (\")");
        }

        if (haveData)
        {
            result.add(current.toString());
        }

        return result;
    }

    /**
     * The inverse of split, which is *not* the same as joining.  Returns a
     * string that if passed to split would return the given list.  This
     * involves quoting any piece that contains a space or is empty, and
     * escaping any quote characters or backslashes.
     *
     * @param pieces pieces of string to unsplit
     * @return the inverse of split, as applied to pieces
     * @see StringUtils#split(String)
     */
    public static String unsplit(List<String> pieces)
    {
        StringBuilder result = new StringBuilder();
        StringBuilder current = new StringBuilder();
        boolean first = true;

        for (String piece : pieces)
        {
            boolean quote = piece.length() == 0;
            current.delete(0, current.length());

            for (int i = 0; i < piece.length(); i++)
            {
                char c = piece.charAt(i);
                switch (c)
                {
                    case'\\':
                    case'\"':
                        current.append('\\');
                        break;
                    case' ':
                        quote = true;
                        break;
                }

                current.append(c);
            }

            if (first)
            {
                first = false;
            }
            else
            {
                result.append(' ');
            }

            if (quote)
            {
                result.append('\"');
            }

            result.append(current);

            if (quote)
            {
                result.append('\"');
            }
        }

        return result.toString();
    }

    public static String join(String glue, String... pieces)
    {
        return join(glue, false, false, pieces);
    }

    public static String join(String glue, boolean glueCheck, String... pieces)
    {
        return join(glue, glueCheck, false, pieces);
    }

    /**
     * Joins the given string pieces together with the glue in all the
     * joins.  The result is <piece1><glue><piece2>...<glue><pieceN>.
     *
     * @param glue      glue string to insert at all the join points
     * @param glueCheck if true, both sides of the join will be stripped of
     *                  any pre-existing glue (i.e. if the first piece ends
     *                  with glue it will be stripped, and if the second
     *                  starts with glue it will be stripped), ensuring
     *                  no duplication of glue at join points
     * @param skipEmpty if true, empty pieces are ignored
     * @param pieces    the pieces of string to join together
     * @return a string made up of the given pieces, joined with the glue
     */
    public static String join(String glue, boolean glueCheck, boolean skipEmpty, String... pieces)
    {
        StringBuilder result = new StringBuilder();

        if (skipEmpty)
        {
            // For total consistency, strip out empty pieces first
            List<String> list = new ArrayList<String>(pieces.length);
            for (String piece : pieces)
            {
                if (piece.length() > 0)
                {
                    list.add(piece);
                }
            }

            pieces = list.toArray(new String[list.size()]);
        }

        for (int i = 0; i < pieces.length; i++)
        {
            String piece = pieces[i];

            if (glueCheck)
            {
                if (i > 0 && piece.startsWith(glue))
                {
                    piece = piece.substring(glue.length());
                }

                if (i < pieces.length - 1 && piece.endsWith(glue))
                {
                    piece = piece.substring(0, piece.length() - glue.length());
                }
            }

            if (i > 0)
            {
                result.append(glue);
            }

            result.append(piece);
        }

        return result.toString();
    }

    public static String join(String separator, Collection<String> parts)
    {
        return join(separator, parts.toArray(new String[parts.size()]));
    }

    /**
     * Encodes the given string in application/x-www-form-urlencoded format.
     * Note that this is *not* the same as encoding the string to be part of
     * a URL itself.
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
            return s;
        }
    }

    /**
     * Encodes the given string as a path to appear in a literal URL (as
     * defined in RFC2396).
     *
     * @param path the path to encode
     * @return encoded form of the path (% escaping for illegal octets)
     */
    public static String urlEncodePath(String path)
    {
        try
        {
            // We need to include a scheme and host because otherwise a
            // double slash at the start of the path confuses URI.
            URI uri = new URI("http", "0.0.0.0", "/" + path, null);
            String encoded = uri.getRawPath();
            if (encoded.startsWith("/"))
            {
                encoded = encoded.substring(1);
            }
            return encoded;
        }
        catch (URISyntaxException e)
        {
            return path;
        }
    }

    public static String pluralise(String singularNoun)
    {
        String pluralNoun = singularNoun;

        int nounLength = pluralNoun.length();

        if (nounLength == 1)
        {
            pluralNoun = pluralNoun + 's';
        }
        else if (nounLength > 1)
        {
            char secondToLastChar = pluralNoun.charAt(nounLength - 2);

            if (pluralNoun.endsWith("y"))
            {
                switch (secondToLastChar)
                {
                    case'a': // fall-through
                    case'e': // fall-through
                    case'i': // fall-through
                    case'o': // fall-through
                    case'u':
                        pluralNoun = pluralNoun + 's';
                        break;
                    default:
                        pluralNoun = pluralNoun.substring(0, nounLength - 1)
                                + "ies";
                }
            }
            else if (pluralNoun.endsWith("s"))
            {
                switch (secondToLastChar)
                {
                    case's':
                        pluralNoun = pluralNoun + "es";
                        break;
                    default:
                        pluralNoun = pluralNoun + "ses";
                }
            }
            else
            {
                pluralNoun = pluralNoun + 's';
            }
        }
        return pluralNoun;
    }

    /**
     * Encodes the given string such that it may be used as a component in a
     * URI (i.e. part of the path in the URI).  Note only a single path
     * component should be passed as the path separator (/) is encoded by
     * this method.  Non-ASCII characters are encoded to UTF-8 and then
     * represented in the result as %-encoded bytes.
     *
     * @see #uriComponentDecode(String)
     *
     * @param in the string to encode
     * @return the input string encoded as a valid URI path component
     */
    public static String uriComponentEncode(String in)
    {
        StringBuilder sb = null;
        for(int i = 0; i < in.length(); i++)
        {
            char c = in.charAt(i);
            if(allowedInURIComponent(c))
            {
                if(sb != null)
                {
                    sb.append(c);
                }
            }
            else
            {
                if (sb == null)
                {
                    sb = new StringBuilder(in.substring(0, i));
                }

                try
                {
                    byte[] bytes = in.substring(i, i + 1).getBytes("UTF-8");
                    for (byte b: bytes)
                    {
                        sb.append('%');
                        sb.append(toHexString(b));
                    }
                }
                catch (UnsupportedEncodingException e)
                {
                    // Programmer error: not handleable
                    throw new RuntimeException(e);
                }
            }
        }

        if(sb == null)
        {
            return in;
        }
        else
        {
            return sb.toString();
        }
    }

    /**
     * Decodes a URI path component back into a Java string.  The input
     * should have all non-ASCII characters and URI-special characters
     * represented as %-encoded bytes.  Sequences of such bytes are decoded
     * using UTF-8 to give a Java string.
     *
     * @see #uriComponentEncode(String)
     *
     * @param in the string to decode
     * @return decoded version of the input string
     */
    public static String uriComponentDecode(String in)
    {
        StringBuilder sb = null;
        byte[] bytes = null;
        int byteOffset = 0;

        for(int i = 0; i < in.length(); i++)
        {
            char c = in.charAt(i);
            if(c == '%')
            {
                if(sb == null)
                {
                    sb = new StringBuilder(in.substring(0, i));
                }

                if(bytes == null)
                {
                    // Max number of escaped octets possible.
                    bytes = new byte[in.length() / 3];
                }

                if(i < in.length() - 2)
                {
                    // Decode to octet
                    try
                    {
                        bytes[byteOffset] = fromHexString(in.substring(i + 1, i + 3));
                        byteOffset++;
                        i += 2;
                    }
                    catch(NumberFormatException e)
                    {
                        sb.append(c);
                    }
                }
                else
                {
                    sb.append(c);
                }
            }
            else
            {
                byteOffset = decodeAndAppendBytes(bytes, byteOffset, sb);
                if(sb != null)
                {
                    sb.append(c);
                }
            }
        }

        if(sb == null)
        {
            return in;
        }
        else
        {
            decodeAndAppendBytes(bytes, byteOffset, sb);
            return sb.toString();
        }
    }

    private static int decodeAndAppendBytes(byte[] bytes, int byteOffset, StringBuilder sb)
    {
        if(byteOffset > 0)
        {
            try
            {
                sb.append(new String(bytes, 0, byteOffset, "UTF-8"));
                byteOffset = 0;
            }
            catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException(e);
            }
        }
        return byteOffset;
    }

    private static boolean allowedInURIComponent(char c)
    {
        if(c < '\u0080')
        {
            // In the ASCII range we only accept alphanumerics and:
            //   -_.!~*'()
            if(Character.isLetterOrDigit(c))
            {
                return true;
            }
            else
            {
                switch(c)
                {
                    case '-':
                    case '_':
                    case '.':
                    case '!':
                    case '~':
                    case '*':
                    case '\'':
                    case '(':
                    case ')':
                        return true;
                    default:
                        return false;
                }
            }
        }
        else
        {
            return false;
        }
    }

    private static String toHexString(byte c)
    {
        return Integer.toString(c < 0 ? c + 256 : c, 16);
    }

    private static byte fromHexString(String s)
    {
        if(s.length() != 2)
        {
            throw new NumberFormatException("Expecting two-character string to conver to a single byte");
        }

        int v = Integer.parseInt(s, 16);
        if(v > Byte.MAX_VALUE)
        {
            return (byte)(v - 256);
        }
        else
        {
            return (byte)v;
        }
    }

    /**
     * Splits the given string around the first occurence of the given
     * separator.
     *
     * @param s         the string to split
     * @param seperator separator character that delimits tokens
     * @param skipEmpty if true, empty tokens are skipped over
     * @return a pair of strings: the next token and the remaining string, or
     *         null if no more tokens are found 
     */
    public static String[] getNextToken(String s, char seperator, boolean skipEmpty)
    {
        if(s.length() == 0)
        {
            return null;
        }
        
        int index = s.indexOf(seperator);
        String token;
        String remainder;
        if(index < 0)
        {
            token = s;
            remainder = "";
        }
        else
        {
            token = s.substring(0, index);
            remainder = s.substring(index + 1);
        }

        if(token.length() == 0 && skipEmpty)
        {
            return getNextToken(s.substring(1), seperator, skipEmpty);
        }
        else
        {
            return new String[]{token, remainder};
        }
    }

    /**
     * Converts the given string to a valid HTML name by replacing invalid
     * name characters with periods ('.').  An 'a' character may also be
     * prepended if the string does not start with an ASCII letter.  Note
     * that this conversion is lossy: two input strings may result in the
     * same output string (e.g. "a:b" and "a/b" both give "a:b").
     *
     * @param s input string to convert
     * @return a valid HTML name string that resembles s as closely as
     *         possible
     */
    public static String toValidHtmlName(String s)
    {
        StringBuilder sb = null;
        if(s.length() == 0 || !isHtmlNameStartChar(s.charAt(0)))
        {
            sb = new StringBuilder(s.length() + 1);
            sb.append('a');
        }

        for(int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            if(isHtmlNameChar(c))
            {
                if(sb != null)
                {
                    sb.append(c);
                }
            }
            else
            {
                if(sb == null)
                {
                    sb = new StringBuilder(s.length());
                    sb.append(s.substring(0, i));
                }

                sb.append('.');
            }
        }

        if(sb == null)
        {
            return s;
        }
        else
        {
            return sb.toString();
        }
    }

    /**
     * @param c character to test
     * @return true iff c is a valid first character for an HTML name
     */
    public static boolean isHtmlNameStartChar(char c)
    {
        return isAsciiAlphabetical(c);
    }

    /**
     * @see #isHtmlNameStartChar
     * @param c character to test
     * @return true iff c is valid character for use in an HTML name (note
     *         that the first character in the name is further restricted)
     */
    public static boolean isHtmlNameChar(char c)
    {
        if(isAsciiAlphaNumeric(c))
        {
            return true;
        }
        else
        {
            switch(c)
            {
                case '-':
                case '_':
                case ':':
                case '.':
                    return true;
                default:
                    return false;
            }
        }
    }

    /**
     * @param c character to test
     * @return true iff c is an ascii letter or digit (a-z, A-Z or 0-9).
     */
    public static boolean isAsciiAlphaNumeric(char c)
    {
        return isAsciiAlphabetical(c) || isAsciiDigit(c);
    }

    /**
     * @param c character to test
     * @return true iff c is an ascii letter (a-z or A-Z).
     */
    public static boolean isAsciiAlphabetical(char c)
    {
        return isAsciiLowerCase(c) || isAsciiUpperCase(c);
    }

    /**
     * @param c character to test
     * @return true iff c is an upper case ascii letter (a-z).
     */
    public static boolean isAsciiUpperCase(char c)
    {
        return c >= 'A' && c <= 'Z';
    }

    /**
     * @param c character to test
     * @return true iff c is a lower case ascii letter (a-z).
     */
    public static boolean isAsciiLowerCase(char c)
    {
        return c >= 'a' && c <= 'z';
    }

    /**
     * @param c character to test
     * @return true iff c is an ascii digit (0-9).
     */
    public static boolean isAsciiDigit(char c)
    {
        return c >= '0' && c <= '9';
    }

    public static String stripLineBreaks(String s)
    {
        return s.replaceAll("\r|\n", "");
    }
}
