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

package com.zutubi.pulse.master.webwork.views.freemarker;

import com.opensymphony.webwork.views.freemarker.FreemarkerManager;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.util.logging.Logger;
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

    private MasterConfigurationManager configManager;

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

    /**
     * Get the set of template loaders used to pick up freemarker templates from the
     * systems custom template roots.
     *
     * @param superLoader   an existing template loader that will be appended to the end
     * of the list so that if the custom template loaders are not able to locate the requested
     * resource then the 'superLoader' will be used.
     *
     * @return an array of configured template loaders that define where and in what order
     * template loaders are queried for resources. 
     */
    private TemplateLoader[] getLoaders(TemplateLoader superLoader)
    {
        SystemPaths paths = configManager.getSystemPaths();

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
        TemplateLoader superLoader = super.getTemplateLoader(servletContext);
        return new MultiTemplateLoader(getLoaders(superLoader));
    }

    public void setConfigurationManager(MasterConfigurationManager configManager)
    {
         this.configManager = configManager;
    }
}
