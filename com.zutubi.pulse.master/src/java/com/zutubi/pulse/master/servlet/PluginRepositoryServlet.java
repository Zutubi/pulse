package com.zutubi.pulse.master.servlet;

import com.google.common.collect.Iterables;
import com.zutubi.pulse.core.plugins.*;
import com.zutubi.pulse.core.plugins.repository.PluginList;
import static com.zutubi.pulse.core.plugins.repository.http.HttpPluginRepository.*;
import com.zutubi.pulse.core.util.PulseZipUtils;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A servlet that implements a plugin repository around the plugin manager.
 * The servlet virtualises a file system to match that expected by a
 * {@link com.zutubi.pulse.core.plugins.repository.http.HttpPluginRepository}.
 */
public class PluginRepositoryServlet extends HttpServlet
{
    private static final Logger LOG = Logger.getLogger(PluginRepositoryServlet.class);

    public static final String PATH_REPOSITORY = "pluginrepository";
    
    private PluginManager pluginManager;
    private SystemPaths systemPaths;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String path = stripPrefix(request.getPathInfo());
        if (path.equals(PATH_AVAILABLE))
        {
            listAvailable(response);
        }
        else if (path.startsWith(PATH_PLUGINS))
        {
            String pluginFileName = path.substring(PATH_PLUGINS.length());
            pluginFileName = StringUtils.stripPrefix(pluginFileName, "/");            
            sendPlugin(pluginFileName, response);
        }
        else
        {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "File not found: " + path);
        }
    }

    private String stripPrefix(String path)
    {
        path = StringUtils.stripPrefix(path, "/");
        path = StringUtils.stripPrefix(path, PATH_REPOSITORY);
        return StringUtils.stripPrefix(path, "/");
    }

    private void listAvailable(HttpServletResponse response) throws IOException
    {
        Iterable<Plugin> runningPlugins = Iterables.filter(pluginManager.getPlugins(), new PluginRunningPredicate());

        ServletOutputStream os = response.getOutputStream();
        PluginList.write(PluginList.toInfos(runningPlugins), os);
        os.flush();
    }
    
    private void sendPlugin(String pluginFileName, HttpServletResponse response) throws IOException
    {
        String idAndVersion;
        if (pluginFileName.endsWith(EXTENSION_JAR))
        {
            idAndVersion = pluginFileName.substring(0, pluginFileName.length() - EXTENSION_JAR.length());
        }
        else
        {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid plugin file name: " + pluginFileName);
            return;
        }
        
        String[] parts = StringUtils.split(idAndVersion, '-');
        if (parts.length != 2)
        {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid plugin file name: " + pluginFileName);
            return;
        }

        Plugin plugin = pluginManager.getPlugin(parts[0]);
        if (plugin == null)
        {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown plugin id: " + parts[0]);
            return;
        }
        
        if (!plugin.getVersion().toString().equals(parts[1]))
        {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Plugin version mismatch, requested: " + parts[1] +", found: " + plugin.getVersion().toString());
            return;
        }
        
        if (plugin instanceof JarFilePlugin)
        {
            uploadPlugin(new File(plugin.getSource()), response);
        }
        else if (plugin instanceof DirectoryPlugin)
        {
            uploadPlugin((DirectoryPlugin) plugin, response);
        }
        else
        {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Cannot upload plugin '" + pluginFileName + "' as it has unsupported type: " + plugin.getClass());
        }
    }

    private void uploadPlugin(File jarFile, HttpServletResponse response) throws IOException
    {
        InputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream(jarFile);
            IOUtils.joinStreams(inputStream, response.getOutputStream());
            response.getOutputStream().flush();
        }
        catch (IOException e)
        {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "I/O error: " + e.getMessage());
        }
        finally
        {
            IOUtils.close(inputStream);
        }
    }

    private synchronized void uploadPlugin(DirectoryPlugin plugin, HttpServletResponse response) throws IOException
    {
        // In development we have plugin directories.  We create jars for them
        // on demand and cache them in the system temp directory.
        File jarFile = new File(systemPaths.getTmpRoot(), plugin.getId() + "-" + plugin.getVersion() + ".jar");
        try
        {
            if (!jarFile.exists())
            {
                File pluginDir = new File(plugin.getSource());
                PulseZipUtils.createZip(jarFile, pluginDir, null);
            }
            uploadPlugin(jarFile, response);
        }
        catch (IOException e)
        {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "I/O error: " + e.getMessage());
        }
    }

    private void sendError(HttpServletResponse response, int code, String message) throws IOException
    {
        LOG.warning(message);
        response.sendError(code, message);
    }

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }
    
    public void setSystemPaths(SystemPaths systemPaths)
    {
        this.systemPaths = systemPaths;
    }
}
