package com.zutubi.pulse.util;

import nu.xom.*;

import java.io.*;

/**
 */
public class XMLUtils
{
    private static final char[] ILLEGAL_CHARACTERS = { '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005',
                                                       '\u0006', '\u0007', '\u0008', '\u000B', '\u000C', '\u000E',
                                                       '\u000F', '\u0010', '\u0011', '\u0012', '\u0013', '\u0014',
                                                       '\u0015', '\u0016', '\u0017', '\u0018', '\u0019', '\u001A',
                                                       '\u001B', '\u001C', '\u001D', '\u001E', '\u001F' };

    public static String prettyPrint(String xmlFile)
    {
        Builder builder = new Builder();
        ByteArrayOutputStream os = null;

        try
        {
            Document doc = builder.build(new StringReader(xmlFile));
            os = new ByteArrayOutputStream();
            Serializer serializer = new Serializer(os);
            serializer.setIndent(4);
            serializer.write(doc);
            serializer.flush();
            return os.toString();
        }
        catch (Exception e)
        {
            // Try our best to pretty print, but not fatal if we can't
            return xmlFile;
        }
        finally
        {
            IOUtils.close(os);
        }
    }

    public static void writeDocument(File file, Document doc) throws IOException
    {
        BufferedOutputStream bos = null;
        try
        {
            FileOutputStream fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);

            Serializer serializer = new Serializer(bos);
            serializer.write(doc);
        }
        finally
        {
            IOUtils.close(bos);
        }
    }

    public static String getText(Element element)
    {
        StringBuffer result = new StringBuffer();
        for(int i = 0; i < element.getChildCount(); i++)
        {
            Node child = element.getChild(i);
            if(child instanceof Text)
            {
                result.append(child.getValue());
            }
        }
        
        return result.toString();
    }

    public static String removeIllegalCharacters(String s)
    {
        StringBuilder builder = null;
        for(int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            if(!isXMLCharacter(c))
            {
                if(builder == null)
                {
                    builder = new StringBuilder(s.length());
                    builder.append(s, 0, i);
                }
            }
            else if(builder != null)
            {
                builder.append(c);
            }
        }

        return builder == null ? s : builder.toString();
    }

    public static boolean isXMLCharacter(int c)
    {
        if (c < 0x20)
        {
            return c == '\n' || c == '\r' || c == '\t';
        }

        return c <= 0xD7FF || c >= 0xE000 && (c <= 0xFFFD || c >= 0x10000 && c <= 0x10FFFF);
    }
}
