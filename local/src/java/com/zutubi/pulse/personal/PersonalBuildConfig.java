package com.zutubi.pulse.personal;

import com.zutubi.pulse.config.CompositeConfig;
import com.zutubi.pulse.config.ConfigSupport;
import com.zutubi.pulse.config.FileConfig;
import com.zutubi.pulse.config.PropertiesConfig;

import java.io.File;

/**
 */
public class PersonalBuildConfig
{
    public static final String PROPERTY_PULSE_URL = "pulse.url";
    public static final String PROPERTY_PULSE_USER = "pulse.user";
    public static final String PROPERTY_PULSE_PASSWORD = "pulse.password";
    private static final String PROPERTY_PROJECT = "project";
    private static final String PROPERTY_SPECIFICATION = "specification";

    private ConfigSupport config;

    public PersonalBuildConfig(File base)
    {
        CompositeConfig composite = new CompositeConfig(new PropertiesConfig(System.getProperties()));
        while(base != null)
        {
            File properties = new File(base, ".pulse.personal.properties");
            if(properties.exists())
            {
                composite.append(new FileConfig(properties));
            }
            base = base.getParentFile();
        }

        config = new ConfigSupport(composite);
    }

    public String getPulseUrl()
    {
        return config.getProperty(PROPERTY_PULSE_URL);
    }

    public String getPulseUser()
    {
        return config.getProperty(PROPERTY_PULSE_USER);
    }

    public String getPulsePassword()
    {
        return config.getProperty(PROPERTY_PULSE_PASSWORD);
    }

    public String getProject()
    {
        return config.getProperty(PROPERTY_PROJECT);
    }

    public String getSpecification()
    {
        return config.getProperty(PROPERTY_SPECIFICATION);
    }
}
