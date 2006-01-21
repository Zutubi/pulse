package com.cinnamonbob.bootstrap.velocity;

import com.cinnamonbob.bootstrap.ApplicationPaths;
import com.cinnamonbob.bootstrap.ConfigUtils;
import com.cinnamonbob.util.logging.Logger;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;

import java.io.File;

/**
 * 
 *
 */
public class BobTemplateVelocityResourceLoader extends FileResourceLoader
{

    private static final Logger LOG = Logger.getLogger(BobTemplateVelocityResourceLoader.class);

    public static String getFullTemplatePath()
    {
        ApplicationPaths paths = ConfigUtils.getManager().getApplicationPaths();

        StringBuffer result = new StringBuffer();
        boolean first = true;

        for (File path : paths.getTemplateRoots())
        {
            if (first)
            {
                first = false;
            }
            else
            {
                result.append(',');
            }
            result.append(path.getAbsolutePath());
        }

        return result.toString();
    }

    public void init(ExtendedProperties configuration)
    {
        // NOTE: the path can be a comma separated list of paths... 
        configuration.setProperty("path", getFullTemplatePath());
        super.init(configuration);
    }

}
