package com.zutubi.pulse.master.transfer.xml;

import nu.xom.Element;
import nu.xom.Serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * A subclass of the nu.xom.Serializer that makes some of the protected methods available to this package.
 *
 */
public class ProxySerializer extends Serializer
{
    public ProxySerializer(OutputStream out)
    {
        super(out);
    }

    public ProxySerializer(OutputStream out, String encoding) throws UnsupportedEncodingException
    {
        super(out, encoding);
    }

    protected void writeXMLDeclaration() throws IOException
    {
        super.writeXMLDeclaration();
    }

    protected void writeStartTag(Element element) throws IOException
    {
        super.writeStartTag(element);
    }

    protected void writeEndTag(Element element) throws IOException
    {
        super.writeEndTag(element);
    }

    protected void write(Element element) throws IOException
    {
        super.write(element);
    }
}
