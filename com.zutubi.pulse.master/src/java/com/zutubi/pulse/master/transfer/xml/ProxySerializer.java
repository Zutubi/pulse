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
