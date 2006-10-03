package com.zutubi.pulse.form.ui.components;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.form.ui.renderers.FreemarkerTemplateRenderer;
import com.zutubi.pulse.form.ui.ComponentRenderer;
import com.zutubi.pulse.form.NoopTextProvider;
import freemarker.cache.TemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.io.StringWriter;
import java.util.List;
import java.util.Arrays;

/**
 * <class-comment/>
 */
public abstract class ComponentTestCase extends PulseTestCase
{
    protected ComponentRenderer renderer;
    protected Writer writer;
    protected FreemarkerTemplateRenderer templateRenderer;

    protected void setUp() throws Exception
    {
        super.setUp();

        writer = new StringWriter();

        Configuration config = new Configuration();
        config.setTemplateLoader(getMultiLoader());
        config.setObjectWrapper(new DefaultObjectWrapper());
        config.addAutoInclude("macro.ftl");

        templateRenderer = new FreemarkerTemplateRenderer();
        templateRenderer.setConfiguration(config);
        templateRenderer.setWriter(writer);

        renderer = new ComponentRenderer();
        renderer.setTemplateRenderer(templateRenderer);
        renderer.setTextProvider(new NoopTextProvider());
    }

    protected void tearDown() throws Exception
    {
        templateRenderer = null;
        renderer = null;
        writer = null;

        super.tearDown();
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


}
