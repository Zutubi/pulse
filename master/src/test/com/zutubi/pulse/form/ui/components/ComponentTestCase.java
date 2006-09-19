package com.zutubi.pulse.form.ui.components;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.form.ui.renderers.FreemarkerRenderer;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.cache.TemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;

/**
 * <class-comment/>
 */
public abstract class ComponentTestCase extends PulseTestCase
{
    protected FreemarkerRenderer renderer;

    protected void setUp() throws Exception
    {
        super.setUp();

        renderer = new FreemarkerRenderer();

        Configuration config = new Configuration();
        config.setTemplateLoader(getMultiLoader());
        config.setObjectWrapper(new DefaultObjectWrapper());
        config.addAutoInclude("macro.ftl");

        renderer.setFreemarkerConfiguration(config);
    }

    private TemplateLoader getMultiLoader()
    {
        File root = getPulseRoot();
        List<String> templateRoots = Arrays.asList("master/src/templates", "master/src/www");

        FileTemplateLoader loaders[] = new FileTemplateLoader[templateRoots.size()];
        for (int i = 0; i < loaders.length; i++)
        {
            try
            {
                loaders[i] = new FileTemplateLoader(new File(root, templateRoots.get(i)));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return new MultiTemplateLoader(loaders);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
}
