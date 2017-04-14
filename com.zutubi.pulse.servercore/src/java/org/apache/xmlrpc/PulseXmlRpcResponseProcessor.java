/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
