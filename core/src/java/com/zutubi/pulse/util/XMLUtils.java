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

    public static String getText(Element element)
    {
        return getText(element, null);
    }

    public static String getText(Element element, String defaultValue)
    {
        if (element.getChildCount() > 0)
        {
            Node child = element.getChild(0);
            if(child != null && child instanceof Text)
            {
                return child.getValue().trim();
            }
        }

        return defaultValue;
    }

    public static String getRequiredAttribute(Element e, String attribute) throws ParsingException
    {
        String result = e.getAttributeValue(attribute);
        if (result == null)
        {
            throw new ParsingException("Invalid " + e.getLocalName() + ": missing required attribute '" + attribute + "'");
        }

        return result;
    }

    public static String getAttributeDefault(Element e, String attribute, String defaultValue) throws ParsingException
    {
        String result = e.getAttributeValue(attribute);
        if (result == null)
        {
            return defaultValue;
        }

        return result;
    }

    public static Document streamToDoc(InputStream in) throws ParsingException, IOException
    {
        try
        {
            Builder builder = new Builder();
            return builder.build(in);
        }
        finally
        {
            IOUtils.close(in);
        }
    }

    public static Document readDocument(File file) throws IOException, ParsingException
    {
        FileInputStream is = null;
        try
        {
            is = new FileInputStream(file);
            return streamToDoc(is);
        }
        finally
        {
            IOUtils.close(is);
        }
    }

    public static void writeDocument(File file, Document doc) throws IOException
    {
        writeDocument(file, doc, false);
    }

    public static void writeDocument(File file, Document doc, boolean prettyPrint) throws IOException
    {
        BufferedOutputStream bos = null;
        try
        {
            FileOutputStream fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);

            Serializer serializer = new Serializer(bos);
            if(prettyPrint)
            {
                serializer.setIndent(4);
            }
            serializer.write(doc);
        }
        finally
        {
            IOUtils.close(bos);
        }
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
