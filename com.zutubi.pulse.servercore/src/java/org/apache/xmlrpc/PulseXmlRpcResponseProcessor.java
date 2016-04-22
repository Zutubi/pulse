package org.apache.xmlrpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Patches {@link XmlRpcResponseProcessor} to use our own {@link org.apache.xmlrpc.XmlWriter}
 * override {@link PulseXmlWriter}.
 */
public class PulseXmlRpcResponseProcessor extends XmlRpcResponseProcessor
{
    @Override
    public byte[] encodeResponse(Object responseParam, String encoding) throws IOException, UnsupportedEncodingException, XmlRpcException
    {
        long now = 0;
        if (XmlRpc.debug)
        {
            now = System.currentTimeMillis();
        }

        try
        {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            PulseXmlWriter writer = new PulseXmlWriter(buffer, encoding);
            writeResponse(responseParam, writer);
            writer.flush();
            return buffer.toByteArray();
        }
        finally
        {
            if (XmlRpc.debug)
            {
                System.out.println("Spent " + (System.currentTimeMillis() - now)
                        + " millis encoding response");
            }
        }
    }
}
