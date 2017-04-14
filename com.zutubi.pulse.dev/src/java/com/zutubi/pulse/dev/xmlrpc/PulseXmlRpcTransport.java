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

package com.zutubi.pulse.dev.xmlrpc;

import org.apache.xmlrpc.DefaultXmlRpcTransport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

/**
 * An override of the default Apache XmlRpcTransport that allows a proxy to be
 * used.  Unfortunately this is not possible via the {@link org.apache.xmlrpc.CommonsXmlRpcTransport}
 * as it's implementation clobbers proxy details set on the passed in
 * HttpClient.
 */
public class PulseXmlRpcTransport extends DefaultXmlRpcTransport
{
    private String proxyHost;
    private int proxyPort;

    public PulseXmlRpcTransport(URL url, String proxyHost, int proxyPort)
    {
        super(url);
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    public InputStream sendXmlRpc(byte[] request) throws IOException
    {
        if (proxyHost == null)
        {
            con = url.openConnection();
        }
        else
        {
            con = url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
        }
        
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        con.setAllowUserInteraction(false);
        con.setRequestProperty("Content-Length", Integer.toString(request.length));
        con.setRequestProperty("Content-Type", "text/xml");
        if (auth != null)
        {
            con.setRequestProperty("Authorization", "Basic " + auth);
        }
        OutputStream out = con.getOutputStream();
        out.write(request);
        out.flush();
        out.close();
        return con.getInputStream();
    }
}
