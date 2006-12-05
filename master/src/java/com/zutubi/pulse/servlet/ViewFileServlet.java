package com.zutubi.pulse.servlet;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.util.IOUtils;

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
        if (buildManager == null)
        {
            buildManager = (BuildManager) ComponentContext.getBean("buildManager");
        }
        if (configurationManager == null)
        {
            configurationManager =  (MasterConfigurationManager) ComponentContext.getBean("configurationManager");
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

        File file = new File(result.getAbsoluteOutputDir(configurationManager.getDataDirectory()), filePath);
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

        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(file);
            IOUtils.joinStreams(fis, httpServletResponse.getOutputStream());
        }
        finally
        {
            // ensure that we close the open file.
            IOUtils.close(fis);
        }
    }
}
