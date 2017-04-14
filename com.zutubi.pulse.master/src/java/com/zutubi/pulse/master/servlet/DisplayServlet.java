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

package com.zutubi.pulse.master.servlet;

import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import com.zutubi.pulse.master.vfs.provider.pulse.AddressableFileObject;
import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <class comment/>
 */
public class DisplayServlet extends HttpServlet
{
    private static final Logger LOG = Logger.getLogger(DisplayServlet.class);

    private FileSystemManager fsManager;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            String path = request.getPathInfo();

            path = path.replace("\\", "/");

            if (path.startsWith("/"))
            {
                path = path.substring(1);
            }

            if (!path.startsWith("pulse://"))
            {
                path = "pulse:///" + path;
            }

            AbstractPulseFileObject pfo;
            try
            {
                pfo = (AbstractPulseFileObject) fsManager.resolveFile(path);
            }
            catch(FileSystemException e)
            {
                // OK, try replacing pluses with spaces
                path = path.replace('+', ' ');
                pfo = (AbstractPulseFileObject) fsManager.resolveFile(path);
            }

            if (!(pfo instanceof AddressableFileObject))
            {
                response.sendError(404, String.format("The path '%s' does not represent an addressable resource.", request.getPathInfo()));
                return;
            }
            
            AddressableFileObject afo = (AddressableFileObject) pfo;
            String url = afo.getUrlPath();
            if (afo.isLocal())
            {
                if (!url.startsWith("/"))
                {
                    url = "/" + url;
                }
                url = request.getContextPath() + url;
            }
            response.sendRedirect(url);
        }
        catch (FileSystemException e)
        {
            LOG.error(e);
            String message = e.getMessage();
            if (message.startsWith("Unknown message with code"))
            {
                message = e.getCode();
            }
            response.sendError(404, message);
        }
        catch (Exception e)
        {
            LOG.error(e);
            throw new ServletException(e);
        }
    }

    public void setFileSystemManager(FileSystemManager fsManager)
    {
        this.fsManager = fsManager;
    }
}
