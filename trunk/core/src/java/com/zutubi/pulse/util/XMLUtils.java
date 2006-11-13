package com.zutubi.pulse.util;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

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

}
