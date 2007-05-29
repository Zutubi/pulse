package com.zutubi.prototype.table;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.SimpleScalar;
import freemarker.core.DelegateBuiltin;
import junit.framework.TestCase;

import java.io.StringWriter;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class RenderTableDescriptorTest extends TestCase
{
    private Template template;

    protected void setUp() throws Exception
    {
        Configuration config = new Configuration();

        TemplateLoader loader = new ClassTemplateLoader(TableDescriptor.class, "");
        config.setTemplateLoader(loader);

        DelegateBuiltin.conditionalRegistration("i18n", "noopMethod");

        template = config.getTemplate("tab.ftl");
    }

    protected void tearDown() throws Exception
    {
        template = null;
    }

    public void testRenderDescriptor() throws Exception
    {
        TableDescriptor descriptor = createSampleDescriptor();

        List<Data> data = new LinkedList<Data>();
        data.add(new Data("a", "b"));
        data.add(new Data("1", "2"));

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("table", descriptor);
        context.put("data", data);
        context.put("noopMethod", new NoopMethod());

        render(context);
    }

    public void testRenderNoData() throws Exception
    {
        TableDescriptor descriptor = createSampleDescriptor();

        List<Data> data = new LinkedList<Data>();

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("table", descriptor);
        context.put("data", data);
        context.put("noopMethod", new NoopMethod());

        render(context);
    }

    public void testRenderSingleColumn() throws Exception
    {
        TableDescriptor descriptor = new TableDescriptor();
        descriptor.add(new ColumnDescriptor("a"));

        List<Data> data = new LinkedList<Data>();
        data.add(new Data("a", "b"));

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("table", descriptor);
        context.put("data", data);
        context.put("noopMethod", new NoopMethod());

        render(context);
    }

    private TableDescriptor createSampleDescriptor()
    {
        TableDescriptor descriptor = new TableDescriptor();
        descriptor.add(new ColumnDescriptor("a"));
        descriptor.add(new ColumnDescriptor("b"));
        descriptor.add("edit");
        descriptor.add("delete");
        return descriptor;
    }

    private void render(Map<String, Object> context) throws Exception
    {
        StringWriter writer = new StringWriter();
        template.process(context, writer);
        System.out.println(writer.toString());
    }

    public static class Data
    {
        private String a;
        private String b;

        public Data(String a, String b)
        {
            this.a = a;
            this.b = b;
        }

        public String getA()
        {
            return a;
        }

        public String getB()
        {
            return b;
        }
    }

    public static class NoopMethod implements TemplateMethodModel
    {
        public NoopMethod()
        {
        }

        public TemplateModel exec(List args) throws TemplateModelException
        {
            return new SimpleScalar((String) args.get(0));
        }
    }
}
