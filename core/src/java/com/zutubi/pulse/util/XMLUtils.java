package com.zutubi.pulse.util;

import nu.xom.*;

import java.io.*;

import com.opensymphony.util.TextUtils;

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
        StringBuilder result = new StringBuilder();
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

    public static String getRequiredText(Element element, boolean trim)
    {
        String text = getText(element);
        if(trim)
        {
            text = text.trim();
        }

        if(!TextUtils.stringSet(text))
        {
            throw new XMLException("Required text missing from element '" + element.getLocalName() + "'");
        }

        return text;
    }

    public static String getText(Element element, String defaultValue)
    {
        String result = getText(element);
        if(TextUtils.stringSet(result))
        {
            return result;
        }
        else
        {
            return defaultValue;
        }
    }

    public static String getChildText(Element element, String childName, String defaultValue)
    {
        Element child = element.getFirstChildElement(childName);
        if(child == null)
        {
            return defaultValue;
        }

        String result = getText(child);
        if(!TextUtils.stringSet(result))
        {
            return defaultValue;
        }

        return result;
    }

    public static String getRequiredChildText(Element element, String childName, boolean trim)
    {
        Element child = getRequiredChild(element, childName);
        return getRequiredText(child, trim);
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

    public static void forEachChild(Element element, String name, UnaryFunction<Element> fn)
    {
        Elements elements = element.getChildElements(name);
        for(int i = 0; i < elements.size(); i++)
        {
            fn.process(elements.get(i));
        }
    }

    public static Element getRequiredChild(Element element, String name)
    {
        Element child = element.getFirstChildElement(name);
        if(child == null)
        {
            throw new XMLException("Required child element '" + name + "' not found for element '" + element.getLocalName() + "'");
        }

        return child;
    }
}
