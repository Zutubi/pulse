/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.servlet;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.model.BuildManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;

/**
 */
public class ViewFileServlet extends HttpServlet
{
    private BuildManager buildManager;

    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException
    {
        if (buildManager == null)
        {
            buildManager = (BuildManager) ComponentContext.getBean("buildManager");
        }

        String path = httpServletRequest.getPathInfo();
        path = path.replace('+', ' ');
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

        File file = new File(result.getOutputDir(), filePath);
        if (filePath.equals(artifact.getName()))
        {
            String index = artifact.findIndexFile();
            if (index == null)
            {
                httpServletResponse.sendError(404);
                return;
            }

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

        IOUtils.joinStreams(new FileInputStream(file), httpServletResponse.getOutputStream());
    }
}
