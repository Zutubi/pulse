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

import com.zutubi.pulse.master.MasterBuildPaths;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.servercore.services.InvalidTokenException;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import com.zutubi.pulse.servercore.servlet.ServletUtils;
import com.zutubi.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 */
public class DownloadPatchServlet extends HttpServlet
{
    private static final Logger LOG = Logger.getLogger(DownloadPatchServlet.class);
    private MasterConfigurationManager configurationManager;
    private ServiceTokenManager serviceTokenManager;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        
        if (configurationManager == null || serviceTokenManager == null)
        {
            throw new UnavailableException("Servlet not ready", 30);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            String token = request.getParameter("token");
            try
            {
                serviceTokenManager.validateToken(token);
            }
            catch (InvalidTokenException e)
            {
                response.sendError(403, "Invalid token");
                return;
            }

            long userId = Long.parseLong(request.getParameter("user"));
            long number = Long.parseLong(request.getParameter("number"));

            MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
            File patchFile = paths.getUserPatchFile(userId, number);

            ServletUtils.sendFile(patchFile, response);
        }
        catch (NumberFormatException e)
        {
            try
            {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid parameter");
            }
            catch (IOException e1)
            {
                LOG.warning(e1);
            }
        }
        catch (IOException e)
        {
            LOG.warning(e);
        }
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }
}
