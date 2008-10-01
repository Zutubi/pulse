package com.zutubi.pulse.bootstrap.velocity;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.SystemPaths;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;

import java.io.File;

/**
 * A velocity resource file loader that uses the application installation
 * directories to lookup velocity templates.
 */
public class PulseTemplateVelocityResourceLoader extends FileResourceLoader
{
    private MasterConfigurationManager configManager;

    public PulseTemplateVelocityResourceLoader()
    {
        // since velocity does not provide an object creation hook,
        // we need to handle the autowiring ourselves.
        SpringComponentContext.autowire(this);
    }

    /**
     * Retrieve a comma separated list of the systems template paths. These paths
     * are represented as absolute paths.
     *
     * @return the template paths added by this loader
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

    public void setConfigurationManager(MasterConfigurationManager configManager)
    {
        this.configManager = configManager;
    }
}
