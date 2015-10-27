package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.BuildProperties;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link RecipePaths} implementation for testing.
 */
public class SimpleRecipePaths implements RecipePaths
{
    private File baseDir;
    private File outputDir;

    public SimpleRecipePaths(File baseDir, File outputDir)
    {
        this.baseDir = baseDir;
        this.outputDir = outputDir;
    }

    public File getCheckoutDir()
    {
        return baseDir;
    }

    public File getBaseDir()
    {
        return baseDir;
    }

    public File getOutputDir()
    {
        return outputDir;
    }

    public Map<String, String> getPathProperties()
    {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(BuildProperties.PROPERTY_BASE_DIR, getBaseDir().getAbsolutePath());
        properties.put(BuildProperties.PROPERTY_OUTPUT_DIR, getOutputDir().getAbsolutePath());
        return properties;
    }
}
