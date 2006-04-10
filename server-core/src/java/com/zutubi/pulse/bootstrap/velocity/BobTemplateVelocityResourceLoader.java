package com.zutubi.pulse.bootstrap.velocity;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.bootstrap.SystemPaths;
import com.zutubi.pulse.util.logging.Logger;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;

import java.io.File;

/**
 * A velocity resource file loader that uses the application installation
 * directories to lookup velocity templates.
 *
 */
public class BobTemplateVelocityResourceLoader extends FileResourceLoader
{
    private static final Logger LOG = Logger.getLogger(BobTemplateVelocityResourceLoader.class);

    private ConfigurationManager configManager;

    public BobTemplateVelocityResourceLoader()
    {
        // since velocity does not provide an object creation hook,
        // we need to handle the autowiring ourselves.
        ComponentContext.autowire(this);
    }

    /**
     * Retrieve a comma separated list of the systems template paths. These paths
     * are represented as absolute paths.
     *
     */
    public String getFullTemplatePath()
    {
        SystemPaths paths = configManager.getSystemPaths();

        StringBuffer result = new StringBuffer();
        String sep = "";
        for (File path : paths.getTemplateRoots())
        {
            result.append(sep);
            result.append(path.getAbsolutePath());
            sep = ",";
        }
        return result.toString();
    }

    public void init(ExtendedProperties configuration)
    {
        // NOTE: the path can be a comma separated list of paths... 
        configuration.setProperty("path", getFullTemplatePath());
        super.init(configuration);
    }

    /**
     * Required resource.
     *
     * @param configManager
     */
    public void setConfigurationManager(ConfigurationManager configManager)
    {
        this.configManager = configManager;
    }
}
