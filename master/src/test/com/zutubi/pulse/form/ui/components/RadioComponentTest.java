package com.zutubi.pulse.form.ui.components;

import junit.framework.TestCase;
import com.zutubi.pulse.form.ui.renderers.FreemarkerRenderer;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.ComponentContext;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.cache.MultiTemplateLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;

/**
 * <class-comment/>
 */
public class RadioComponentTest extends ComponentTestCase
{
    private RadioComponent radio;

    protected void setUp() throws Exception
    {
        super.setUp();

        radio = new RadioComponent();
    }

    protected void tearDown() throws Exception
    {
        radio = null;

        super.tearDown();
    }

    public void testComponentRendering()
    {
        radio.setList(Arrays.asList("plain", "html"));
        radio.setValue("plain");
        radio.setName("contact.type");
        radio.setLabel("Email format");
        radio.render(renderer);
        assertFalse(renderer.hasError());
        System.out.println(renderer.getRenderedContent());
    }
}
