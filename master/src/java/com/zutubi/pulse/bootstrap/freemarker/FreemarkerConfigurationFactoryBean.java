package com.zutubi.pulse.bootstrap.freemarker;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.util.logging.Logger;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import org.springframework.beans.factory.FactoryBean;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 */
public class FreemarkerConfigurationFactoryBean implements FactoryBean
{
    private static final Logger LOG = Logger.getLogger(FreemarkerConfigurationFactoryBean.class);

    private static Configuration FREEMARKER_CONFIGURATION;

    public Object getObject() throws Exception
    {
        if (FREEMARKER_CONFIGURATION == null)
        {
            synchronized (this)
            {
                if (FREEMARKER_CONFIGURATION == null)
                {
                    FREEMARKER_CONFIGURATION = new Configuration();
                    FREEMARKER_CONFIGURATION.setTemplateLoader(getMultiLoader());
                    FREEMARKER_CONFIGURATION.setObjectWrapper(new DefaultObjectWrapper());
                    FREEMARKER_CONFIGURATION.addAutoInclude("macro.ftl");
                }
            }
        }
        return FREEMARKER_CONFIGURATION;
    }

    private TemplateLoader getMultiLoader()
    {
        MasterConfigurationManager manager = (MasterConfigurationManager) ComponentContext.getBean("configurationManager");

        List<File> templateRoots = manager.getSystemPaths().getTemplateRoots();
        FileTemplateLoader loaders[] = new FileTemplateLoader[templateRoots.size()];

        for (int i = 0; i < loaders.length; i++)
        {
            try
            {
                loaders[i] = new FileTemplateLoader(templateRoots.get(i));
            }
            catch (IOException e)
            {
                LOG.severe("Unable to add template root to freemarker configuration: " + e.getMessage(), e);
            }
        }

        return new MultiTemplateLoader(loaders);
    }

    public Class getObjectType()
    {
        return Configuration.class;
    }

    public boolean isSingleton()
    {
        return true;
    }
}
