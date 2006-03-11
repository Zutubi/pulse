package com.cinnamonbob.servlet;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.StoredArtifact;
import com.cinnamonbob.core.model.StoredFileArtifact;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.model.BuildManager;

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
