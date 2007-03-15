package com.zutubi.pulse.util;

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
        if (element.getChildCount() > 0)
        {
            Node child = element.getChild(0);
            if(child != null && child instanceof Text)
            {
                return child.getValue().trim();
            }
        }

        return null;
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
}
