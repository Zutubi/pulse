package com.cinnamonbob.freemarker;

import com.cinnamonbob.bootstrap.ConfigUtils;
import com.cinnamonbob.bootstrap.SystemPaths;
import com.cinnamonbob.util.logging.Logger;
import com.opensymphony.webwork.views.freemarker.FreemarkerManager;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 */
public class CustomFreemarkerManager extends FreemarkerManager
{
    private static final Logger LOG = Logger.getLogger(CustomFreemarkerManager.class);

    public static void initialiseLogging()
    {
        try
        {
            freemarker.log.Logger.selectLoggerLibrary(freemarker.log.Logger.LIBRARY_JAVA);
        }
        catch (ClassNotFoundException e)
        {
            LOG.severe("Unable to set freemarker logger: " + e.getMessage(), e);
        }
    }

    public static TemplateLoader[] getLoaders(TemplateLoader superLoader)
    {
        SystemPaths paths = ConfigUtils.getManager().getSystemPaths();

        List<File> templateRoots = paths.getTemplateRoots();
        TemplateLoader loaders[] = new TemplateLoader[templateRoots.size() + 1];

        for (int i = 0; i < templateRoots.size(); i++)
        {
            try
            {
                loaders[i] = new FileTemplateLoader(templateRoots.get(i));
            }
            catch (IOException e)
            {
                // Programmer error
                LOG.severe(e);
            }
        }

        loaders[loaders.length - 1] = superLoader;
        return loaders;
    }

    protected TemplateLoader getTemplateLoader(ServletContext servletContext)
    {
        return new MultiTemplateLoader(getLoaders(super.getTemplateLoader(servletContext)));
    }
}
