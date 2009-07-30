package com.zutubi.pulse.core.scm.patch;

import com.zutubi.util.config.ConfigSupport;
import com.zutubi.util.config.FileConfig;

import java.io.File;

/**
 * Handles the storage and manipulation of extra properties stored with patch
 * files.
 */
public class PatchProperties
{
    private static final String PROPERTY_FORMAT = "patch.format";

    private ConfigSupport properties;

    public PatchProperties(File file)
    {
        properties = new ConfigSupport(new FileConfig(file));
    }

    public String getPatchFormat()
    {
        return properties.getProperty(PROPERTY_FORMAT, DefaultPatchFormatFactory.FORMAT_STANDARD);
    }

    public void setPatchFormat(String format)
    {
        properties.setProperty(PROPERTY_FORMAT, format);
    }
}
