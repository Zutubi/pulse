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
}
