package com.zutubi.pulse.servlet;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.spring.SpringComponentContext;
import com.zutubi.pulse.bootstrap.SystemPaths;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 */
public class DownloadPackageServlet extends HttpServlet
{
    private static final Logger LOG = Logger.getLogger(DownloadPackageServlet.class);

    private SystemPaths systemPaths;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            String packageName = request.getPathInfo();
            if(packageName == null || packageName.length() == 0)
            {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if(packageName.startsWith("/"))
            {
                packageName = packageName.substring(1);
            }

            File packageFile = getPackageFile(getSystemPaths(), packageName);

            try
            {
                response.setContentType("application/x-octet-stream");
                response.setContentLength((int) packageFile.length());

                FileInputStream input = null;

                try
                {
                    input = new FileInputStream(packageFile);
                    IOUtils.joinStreams(input, response.getOutputStream());
                }
                finally
                {
                    IOUtils.close(input);
                }

                response.getOutputStream().flush();
            }
            catch (FileNotFoundException e)
            {
                LOG.warning(e);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found: " + e.getMessage());
            }
            catch (IOException e)
            {
                LOG.warning(e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "I/O error: " + e.getMessage());
            }
        }
        catch (IOException e)
        {
            LOG.warning(e);
        }
    }

    public static File getAgentZip(SystemPaths systemPaths)
    {
        return getPackageFile(systemPaths, "pulse-agent-" + Version.getVersion().getVersionNumber() + ".zip");
    }

    public static File getPackageFile(SystemPaths systemPaths, String packageName)
    {
        return new File(getPackageDir(systemPaths), packageName);
    }

    public static File getPackageDir(SystemPaths systemPaths)
    {
        return new File(systemPaths.getSystemRoot(), "packages");
    }

    private SystemPaths getSystemPaths()
    {
        if(systemPaths == null)
        {
            ConfigurationManager configurationManager = (ConfigurationManager) SpringComponentContext.getBean("configurationManager");
            systemPaths = configurationManager.getSystemPaths();
        }

        return systemPaths;
    }

    public static String getPackagesUrl(String masterUrl)
    {
        return masterUrl + "/packages";
    }
}
