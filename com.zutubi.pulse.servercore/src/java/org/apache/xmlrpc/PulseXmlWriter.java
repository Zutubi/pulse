package org.apache.xmlrpc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Patches {@link XmlWriter} to correctly handle surrogate pairs in Strings (CIB-3344).
 */
class PulseXmlWriter extends XmlWriter
{
    /**
     * Creates a new instance.
     *
     * @param out The stream to write output to.
     * @param enc The encoding to using for outputing XML.  Only UTF-8
     *            and UTF-16 are supported.  If another encoding is specified,
     *            UTF-8 will be used instead for widest XML parser
     *            interoperability.
     * @throws UnsupportedEncodingException Since unsupported
     *                                      encodings are internally converted to UTF-8, this should only
     *                                      be seen as the result of an internal error.
     */
    PulseXmlWriter(OutputStream out, String enc) throws UnsupportedEncodingException
    {
        super(out, enc);
    }

    @Override
    protected void chardata(String text) throws XmlRpcException, IOException
    {
        int l = text.codePointCount(0, text.length());
        for (int i = 0; i < l; i = text.offsetByCodePoints(i, 1))
        {
            int c = text.codePointAt (i);
            switch (c)
            {
                case '\t':
                case '\n':
                    write(c);
                    break;
                case '\r':
                    // Avoid normalization of CR to LF.
                    writeCharacterReference(c);
                    break;
                case '<':
                    write(LESS_THAN_ENTITY);
                    break;
                case '>':
                    write(GREATER_THAN_ENTITY);
                    break;
                case '&':
                    write(AMPERSAND_ENTITY);
                    break;
                default:
                    // Though the XML spec requires XML parsers to support
                    // Unicode, not all such code points are valid in XML
                    // documents.  Additionally, previous to 2003-06-30
                    // the XML-RPC spec only allowed ASCII data (in
                    // <string> elements).  For interoperability with
                    // clients rigidly conforming to the pre-2003 version
                    // of the XML-RPC spec, we entity encode characters
                    // outside of the valid range for ASCII, too.
                    if (c > 0x7f || !isValidXMLChar(c))
                    {
                        // Replace the code point with a character reference.
                        writeCharacterReference(c);
                    }
                    else
                    {
                        write(c);
                    }
            }
        }
    }

    private void writeCharacterReference(int c)
            throws IOException
    {
        write("&#");
        write(String.valueOf(c));
        write(';');
    }

    /**
     * Section 2.2 of the XML spec describes which Unicode code points
     * are valid in XML:
     *
     * <blockquote><code>#x9 | #xA | #xD | [#x20-#xD7FF] |
     * [#xE000-#xFFFD] | [#x10000-#x10FFFF]</code></blockquote>
     *
     * Code points outside this set must be entity encoded to be
     * represented in XML.
     *
     * @param c The character to inspect.
     * @return Whether the specified character is valid in XML.
     */
    private static boolean isValidXMLChar(int c)
    {
        switch (c)
        {
            case 0x9:
            case 0xa:  // line feed, '\n'
            case 0xd:  // carriage return, '\r'
                return true;

            default:
                return ( (0x20 <= c && c <= 0xd7ff) ||
                        (0xe000 <= c && c <= 0xfffd) ||
                        (0x10000 <= c && c <= 0x10ffff) );
        }
    }
}
