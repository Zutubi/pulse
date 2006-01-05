package com.cinnamonbob.servlet;

import com.cinnamonbob.ServerRecipePaths;
import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.core.util.IOUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 */
public class DownloadResultsServlet extends HttpServlet
{
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        String id = request.getParameter("recipe");

        try
        {
            long recipeId = Long.parseLong(id);
            boolean output = Boolean.parseBoolean(request.getParameter("output"));

            // lookup the recipe location, zip it up and write to output.
            ServerRecipePaths paths = new ServerRecipePaths(recipeId, (ConfigurationManager) ComponentContext.getBean("configurationManager"));
            File dir;
            File zipFile;

            if (output)
            {
                dir = paths.getOutputDir();
                zipFile = paths.getOutputZip();
            }
            else
            {
                dir = paths.getWorkDir();
                zipFile = paths.getWorkZip();
            }

            try
            {
                FileSystemUtils.createZip(zipFile, dir, dir);
                response.setContentType("application/x-octet-stream");
                IOUtils.joinStreams(new FileInputStream(zipFile), response.getOutputStream());
            }
            catch (FileNotFoundException e)
            {
                response.sendError(404, "File not found: " + e.getMessage());
            }
            catch (IOException e)
            {
                response.sendError(500, "I/O error: " + e.getMessage());
            }

        }
        catch (NumberFormatException e)
        {
            try
            {
                response.sendError(500, "Invalid recipe '" + id + "'");
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
