package com.zutubi.pulse.bootstrap.freemarker;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.webwork.mapping.Urls;
import com.zutubi.util.logging.Logger;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModelException;
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
                    FREEMARKER_CONFIGURATION = createConfiguration(getConfigurationManager());
                }
            }
        }
        return FREEMARKER_CONFIGURATION;
    }

    public static Configuration createConfiguration(MasterConfigurationManager configurationManager) throws TemplateModelException
    {
        Configuration configuration = new Configuration();
        configuration.setTemplateLoader(getMultiLoader(configurationManager));
        configuration.setObjectWrapper(new DefaultObjectWrapper());
        configuration.addAutoInclude("macro.ftl");
        String base = configurationManager.getSystemConfig().getContextPathNormalised();
        configuration.setSharedVariable("base", base);
        configuration.setSharedVariable("urls", new Urls(base));
        return configuration;
    }

    public static Configuration createConfiguration(Class clazz, MasterConfigurationManager configurationManager) throws TemplateModelException
    {
        Configuration configuration = createConfiguration(configurationManager);
        TemplateLoader currentLoader = configuration.getTemplateLoader();
        TemplateLoader classLoader = new ClassTemplateLoader(clazz, "");
        MultiTemplateLoader loader = new MultiTemplateLoader(new TemplateLoader[]{ classLoader, currentLoader });
        configuration.setTemplateLoader(loader);
        return configuration;
    }

    private static TemplateLoader getMultiLoader(MasterConfigurationManager configurationManager)
    {
        List<File> templateRoots = configurationManager.getSystemPaths().getTemplateRoots();
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

    private MasterConfigurationManager getConfigurationManager()
    {
        return (MasterConfigurationManager) ComponentContext.getBean("configurationManager");
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
