package com.zutubi.pulse.master.bootstrap.freemarker;

import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.logging.Logger;
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

    private MasterConfigurationManager configurationManager;

    public Object getObject() throws Exception
    {
        if (FREEMARKER_CONFIGURATION == null)
        {
            synchronized (this)
            {
                if (FREEMARKER_CONFIGURATION == null)
                {
                    Configuration configuration = new Configuration();
                    configuration.setTemplateLoader(getMultiLoader(configurationManager.getSystemPaths().getTemplateRoots()));
                    configuration.setObjectWrapper(new DefaultObjectWrapper());
                    configuration.addAutoInclude("macro.ftl");
                    String base = configurationManager.getSystemConfig().getContextPathNormalised();
                    configuration.setSharedVariable("base", base);
                    configuration.setSharedVariable("urls", new Urls(base));

                    FREEMARKER_CONFIGURATION = configuration;
                }
            }
        }
        return FREEMARKER_CONFIGURATION;
    }

    private static TemplateLoader getMultiLoader(List<File> paths)
    {
        FileTemplateLoader loaders[] = new FileTemplateLoader[paths.size()];

        for (int i = 0; i < loaders.length; i++)
        {
            try
            {
                loaders[i] = new FileTemplateLoader(paths.get(i));
            }
            catch (IOException e)
            {
                LOG.severe("Unable to add template root to freemarker configuration: " + e.getMessage(), e);
            }
        }
        return new MultiTemplateLoader(loaders);
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
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
