package com.zutubi.pulse.core.util;

import com.opensymphony.util.TextUtils;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.io.IOUtils;
import nu.xom.*;

import java.io.*;

/**
 */
public class XMLUtils
{
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
        return getText(element, defaultValue, true);   
    }

    public static String getText(Element element, String defaultValue, boolean trim)
    {
        if (element.getChildCount() > 0)
        {
            Node child = element.getChild(0);
            if(child != null && child instanceof Text)
            {
                String value = child.getValue();
                if(trim)
                {
                    value = value.trim();
                }

                return value;
            }
        }

        return defaultValue;
    }

    public static String getRequiredText(Element element, boolean trim)
    {
        String text = getText(element, null, trim);
        if(!TextUtils.stringSet(text))
        {
            throw new XMLException("Required text missing from element '" + element.getLocalName() + "'");
        }

        return text;
    }

    public static String getChildText(Element element, String childName, String defaultValue)
    {
        Element child = element.getFirstChildElement(childName);
        if(child == null)
        {
            return defaultValue;
        }

        return getText(child, defaultValue);
    }

    public static String getRequiredChildText(Element element, String childName, boolean trim)
    {
        Element child = getRequiredChild(element, childName);
        return getRequiredText(child, trim);
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

    public static String getAttributeDefault(Element e, String attribute, String defaultValue)
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

    public static void forEachChild(Element element, String name, UnaryProcedure<Element> fn)
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

    /**
     * Escapes XML special characters in the given string, converting it to a
     * form suitable for direct inclusion in an XML document.  For example,
     * &lt; will be replaced with &amp;lt;.
     *
     * @param s the string to escape
     * @return the string with all XML special characters escaped
     * @throws IllegalCharacterDataException if the input string contains
     *         characters that cannot be represented in XML
     */
    public static String escape(String s)
    {
        return new Text(s).toXML();
    }
}
