package com.zutubi.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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

    public static String encodeAndJoin(char separator, Collection<String> pieces)
    {
        return encodeAndJoin(separator, pieces.toArray(new String[pieces.size()]));
    }

    public static String encodeAndJoin(final char separator, String... pieces)
    {
        return encodeAndJoin(new Predicate<Character>()
        {
            public boolean satisfied(Character character)
            {
                return !character.equals(separator) && !character.equals('%');
            }
        }, separator, pieces);
    }

    /**
     * @see #encodeAndJoin(com.zutubi.util.Predicate, char, String[])
     *
     * @param allowedCharacters a condition that is true for characters that
     *                          are allowed in the result verbatim (i.e. not
     *                          encoded)
     * @param separator         used as the glue when joining the encoded
     *                          strings, must be a disallowed character
     * @param pieces            strings to encode and join
     * @return the given strings encoded and joined together using the given
     *         separator
     */
    public static String encodeAndJoin(Predicate<Character> allowedCharacters, char separator, Collection<String> pieces)
    {
        return encodeAndJoin(allowedCharacters, separator, pieces.toArray(new String[pieces.size()]));
    }

    /**
     * Encodes each of the given strings then joins them with the given
     * separator.
     *
     * @see #splitAndDecode(char, String)
     *
     * @param allowedCharacters a condition that is true for characters that
     *                          are allowed in the result verbatim (i.e. not
     *                          encoded)
     * @param separator         used as the glue when joining the encoded
     *                          strings, must be a disallowed character
     * @param pieces            strings to encode and join
     * @return the given strings encoded and joined together using the given
     *         separator
     */
    public static String encodeAndJoin(Predicate<Character> allowedCharacters, char separator, String... pieces)
    {
        StringBuilder result = new StringBuilder(pieces.length * 32);
        boolean first = true;
        for(String s: pieces)
        {
            if(first)
            {
                first = false;
            }
            else
            {
                result.append(separator);
            }

            result.append(percentEncode(s, allowedCharacters));
        }

        return result.toString();
    }

    /**
     * The inverse of {@link #encodeAndJoin(com.zutubi.util.Predicate, char, String[])}
     * Splits the given string at occurences of the given separator then
     * decodes the resulting pieces.
     *
     * @see #encodeAndJoin (String, String[])
     *
     * @param separator character to split on
     * @param s         the string to be split
     * @return the pieces derived by splitting at the separator and decoding
     *         each piece
     */
    public static List<String> splitAndDecode(char separator, String s)
    {
        String[] pieces = s.split(Character.toString(separator));
        List<String> result = new ArrayList<String>(pieces.length);
        for(String item: pieces)
        {
            result.add(uriComponentDecode(item));
        }
        return result;
    }

    /**
     * Encodes the given string based on the given set of allow characters.
     * Non-allowed characters are encoded to UTF-8 and then represented in
     * the result as %-encoded bytes.
     *
     * @see #uriComponentDecode(String)
     *
     * @param in                the string to encode
     * @param allowedCharacters a condition that is true for characters that
     *                          are allowed in the result verbatim (i.e. not
     *                          encoded)
     * @return the input string encoded as a valid URI path component
     */
    public static String percentEncode(String in, Predicate<Character> allowedCharacters)
    {
        return encode('%', in, allowedCharacters);
    }

    /**
     * Encode the given string based on the given set of allowed characters.
     * Non-allowed characters are encoded to UTF-8 and then represented in the
     * result as encoded bytes, tagged by the given tag.
     *
     * @param tag               the tag used to mark encoded bytes
     * @param in                the string to encode
     * @param allowedCharacters a condition that is true for characters that are
     *                          allowed in the result verbatim (i.e. not encoded)
     *
     * @return  the input string encoded.
     */
    public static String encode(char tag, String in, Predicate<Character> allowedCharacters)
    {
        StringBuilder sb = null;
        for(int i = 0; i < in.length(); i++)
        {
            char c = in.charAt(i);
            if(allowedCharacters.satisfied(c))
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
                        sb.append(tag);
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
     * Decodes a percent-encoded string into a Java string.  The decode
     * searches for %-encoded bytes, sequences of such bytes are decoded
     * using UTF-8 to give a Java string.
     *
     * @see #percentEncode(String, com.zutubi.util.Predicate)
     *
     * @param in the string to decode
     * @return decoded version of the input string
     */
    public static String percentDecode(String in)
    {
        return decode('%', in);
    }

    /**
     * Decodes an encoded string into a java string.  The decode
     * searches for encoded bytes that are marked by the specified tag,
     * sequences of such bytes are decoded using UTF-8 to give a
     * Java string.
     *
     * @param tag   the tag marking encoded bytes.
     * @param in    the string to decode
     * @return  a decoded version of the input string
     */
    public static String decode(char tag, String in)
    {
        StringBuilder sb = null;
        byte[] bytes = null;
        int byteOffset = 0;

        for(int i = 0; i < in.length(); i++)
        {
            char c = in.charAt(i);
            if(c == tag)
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

    /**
     * Encodes the given URI path by applying percent encoding to each of its
     * components.  The path is first split on / before applying {@link #uriComponentEncode(String)}
     * to each of its components and then reassembling it.
     *
     * @param rawPath the path to encode, should be raw components joined with literal forward
     *                slashes
     * @return the decoded form of the path
     */
    public static String uriPathEncode(String rawPath)
    {
        List<String> components = CollectionUtils.map(splitPath(rawPath), new Mapping<String, String>()
        {
            public String map(String s)
            {
                return uriComponentEncode(s);
            }
        });

        return StringUtils.join("/", components);
    }

    /**
     * Decodes the given URI path by applying percent decoding to each of its
     * components.  The path is first split on / before applying {@link #uriComponentDecode(String)}
     * to each of its components and then reassembling it.
     *
     * @param encodedPath the path to decode, should be encoded components
     *                    joined with literal forward slashes
     * @return the decoded form of the path
     */
    public static String uriPathDecode(String encodedPath)
    {
        List<String> components = CollectionUtils.map(splitPath(encodedPath), new Mapping<String, String>()
        {
            public String map(String s)
            {
                return uriComponentDecode(s);
            }
        });

        return StringUtils.join("/", components);
    }

    private static List<String> splitPath(String path)
    {
        // Normal splitting does not handle leading separators as we need it
        // to, so we do it ourselves.
        int offset = 0;
        List<String> components = new LinkedList<String>();
        for (int i = 0; i < path.length(); i++)
        {
            if (path.charAt(i) == '/')
            {
                components.add(path.substring(offset, i));
                offset = i + 1;
            }
        }

        components.add(path.substring(offset));
        return components;
    }

    /**
     * Encodes the given string such that it may be used as a component in a
     * URI (i.e. part of the path in the URI).  Note only a single path
     * component should be passed as the path separator (/) is encoded by
     * this method.  Non-ASCII characters are encoded to UTF-8 and then
     * represented in the result as %-encoded bytes.
     *
     * @see #percentEncode(String, com.zutubi.util.Predicate)
     * @see #uriComponentDecode(String)
     *
     * @param in the string to encode
     * @return the input string encoded as a valid URI path component
     */
    public static String uriComponentEncode(String in)
    {
        return percentEncode(in, new Predicate<Character>()
        {
            public boolean satisfied(Character character)
            {
                return allowedInURIComponent(character);
            }
        });
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
        return percentDecode(in);
    }

    public static int decodeAndAppendBytes(byte[] bytes, int byteOffset, StringBuilder sb)
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

    public static boolean allowedInURI(String str)
    {
        for(int i = 0; i < str.length(); i++)
        {
            char c = str.charAt(i);
            if(!allowedInURIComponent(c))
            {
                return false;
            }
        }
        return true;
    }

    public static boolean allowedInURIComponent(char c)
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

    public static String toHexString(byte c)
    {
        return Integer.toString(c < 0 ? c + 256 : c, 16);
    }

    public static byte fromHexString(String s)
    {
        if(s.length() != 2)
        {
            throw new NumberFormatException("Expecting two-character string to convert to a single byte");
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
     * Converts the given string to a valid HTML name by replacing invalid
     * name characters with periods ('.').  An 'a' character may also be
     * prepended if the string does not start with an ASCII letter.  Note
     * that this conversion is lossy: two input strings may result in the
     * same output string (e.g. "a!b" and "a/b" both give "a.b").
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
        return StringUtils.isAsciiAlphabetical(c);
    }

    /**
     * @see #isHtmlNameStartChar
     * @param c character to test
     * @return true iff c is valid character for use in an HTML name (note
     *         that the first character in the name is further restricted)
     */
    public static boolean isHtmlNameChar(char c)
    {
        if(StringUtils.isAsciiAlphaNumeric(c))
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
}
