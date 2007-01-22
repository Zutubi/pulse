package com.zutubi.pulse.form.ui.components;

import com.zutubi.pulse.form.NoopTextProvider;
import com.zutubi.pulse.form.ui.RenderContext;
import com.zutubi.pulse.form.ui.renderers.FreemarkerTemplateRenderer;
import com.zutubi.pulse.test.PulseTestCase;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

/**
 * <class-comment/>
 */
public abstract class ComponentTestCase extends PulseTestCase
{
//    protected ComponentRenderer renderer;
    protected Writer writer;
    protected FreemarkerTemplateRenderer templateRenderer;
    protected RenderContext context;

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

//        renderer = new ComponentRenderer();

        context = new RenderContext(templateRenderer, new NoopTextProvider());
        context.setWriter(writer);

//        renderer.setContext(context);
    }

    protected void tearDown() throws Exception
    {
        templateRenderer = null;
        context = null;
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
