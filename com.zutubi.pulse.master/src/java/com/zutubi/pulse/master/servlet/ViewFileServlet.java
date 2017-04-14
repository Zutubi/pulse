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

import com.google.common.io.ByteStreams;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.util.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;

/**
 * @deprecated replaced by the FileServlet which is based upon the vfs system.
 */
public class ViewFileServlet extends HttpServlet
{
    private BuildManager buildManager;
    private MasterConfigurationManager configurationManager;

    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException
    {
        String path = httpServletRequest.getPathInfo();
        String [] parts = path.split("/", 4);

        if (parts.length != 4)
        {
            httpServletResponse.sendError(404);
            return;
        }

        long resultId;
        long artifactId;

        try
        {
            resultId = Long.parseLong(parts[1]);
            artifactId = Long.parseLong(parts[2]);
        }
        catch (NumberFormatException e)
        {
            httpServletResponse.sendError(404);
            return;
        }

        CommandResult result = buildManager.getCommandResult(resultId);
        if (result == null)
        {
            httpServletResponse.sendError(404);
            return;
        }

        StoredArtifact artifact = buildManager.getArtifact(artifactId);
        if (artifact == null)
        {
            httpServletResponse.sendError(404);
            return;
        }

        String filePath = parts[3];
        if (filePath.endsWith("/"))
        {
            filePath = filePath.substring(0, filePath.length() - 1);
        }

        File file = new File(result.getAbsoluteOutputDir(configurationManager.getDataDirectory()), filePath);
        if (filePath.equals(artifact.getName()))
        {
            if (!artifact.hasIndexFile())
            {
                httpServletResponse.sendError(404);
                return;
            }

            String index = artifact.findIndexFile();
            filePath = filePath + "/" + index;
            file = new File(file, index);
        }

        StoredFileArtifact fileArtifact = artifact.findFile(filePath);
        if (fileArtifact != null && fileArtifact.getType() != null)
        {
            httpServletResponse.setContentType(fileArtifact.getType());
        }
        else
        {
            httpServletResponse.setContentType(URLConnection.guessContentTypeFromName(filePath));
        }

        if (!file.isFile())
        {
            httpServletResponse.sendError(404);
            return;
        }

        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(file);
            ByteStreams.copy(fis, httpServletResponse.getOutputStream());
        }
        finally
        {
            // ensure that we close the open file.
            IOUtils.close(fis);
        }
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
