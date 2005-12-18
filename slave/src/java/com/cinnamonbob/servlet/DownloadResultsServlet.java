package com.cinnamonbob.servlet;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.slave.RecipeProcessorPaths;

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
        try
        {
            long recipeId = Long.parseLong(request.getParameter("recipe"));

            // lookup the recipe location, zip it up and write to output.
            RecipeProcessorPaths paths = new RecipeProcessorPaths((ConfigurationManager) ComponentContext.getBean("configurationManager"));
            File outputDir = paths.getOutputDir(recipeId);
            File zipFile = paths.getOutputZip(recipeId);
            try
            {
                FileSystemUtils.createZip(zipFile, outputDir, outputDir);
                response.setContentType("application/x-octet-stream");
                IOUtils.joinStreams(new FileInputStream(zipFile), response.getOutputStream());
            }
            catch (FileNotFoundException e)
            {
                response.sendError(404, "File not found: " + e.getMessage());
            }
            catch (IOException e)
            {
                response.sendError(500, "File not found: " + e.getMessage());
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
