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

package com.zutubi.pulse.servercore.xmlrpc;

import org.apache.xmlrpc.XmlRpcServer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class XmlRpcServlet extends HttpServlet
{
    protected XmlRpcServer xmlrpc = null;

    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        xmlrpc = new PulseXmlRpcServer();
    }

    public void destroy()
    {
        super.destroy();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        byte[] result = xmlrpc.execute(request.getInputStream());
        response.setContentType("text/xml");
        response.setContentLength(result.length);
        OutputStream os = response.getOutputStream();
        os.write(result);
        os.flush();
        os.close();
    }
}
