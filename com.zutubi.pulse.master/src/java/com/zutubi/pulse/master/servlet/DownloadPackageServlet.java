package com.zutubi.pulse.master.servlet;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.pulse.servercore.servlet.ServletUtils;
import com.zutubi.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

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
            if (packageName == null || packageName.length() == 0)
            {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if (packageName.startsWith("/"))
            {
                packageName = packageName.substring(1);
            }

            File packageFile = getPackageFile(systemPaths, packageName);

            ServletUtils.sendFile(packageFile, response);
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

    public void setConfigurationManager(ConfigurationManager manager)
    {
        systemPaths = manager.getSystemPaths();
    }

    public static String getPackagesUrl(String masterUrl)
    {
        return masterUrl + "/packages";
    }
}
