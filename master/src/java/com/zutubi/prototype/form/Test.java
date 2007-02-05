package com.zutubi.prototype.form;

import com.zutubi.prototype.mock.Sample;
import com.zutubi.prototype.freemarker.GetTextMethod;
import com.zutubi.pulse.prototype.record.RecordTypeRegistry;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.core.DelegateBuiltin;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class Test
{
    public static void main(String[] argv) throws IOException, TemplateException, IntrospectionException, IllegalAccessException, InstantiationException, ClassNotFoundException
    {
        // setup freemarker resources.
        Configuration config = new Configuration();
        config.setTemplateLoader(getMultiLoader());
        config.setObjectWrapper(new DefaultObjectWrapper());
//        config.addAutoInclude("macro.ftl");

        FormDescriptorFactory factory = new FormDescriptorFactory();
        
        FormDescriptor formDescriptor = factory.createDescriptor(Sample.class);

        Sample instance = new Sample();
        instance.setAddress("address");
        instance.setEmail("email");
        instance.setName("name");
        instance.setPassword("password");

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("form", formDescriptor.instantiate(instance));
        // configure the getTextMethod with details of how to look up the I18N strings.
        context.put("i18nText", new GetTextMethod());

        // provide some syntactic sweetener by linking the i18n text method to the ?i18n builtin function.
        DelegateBuiltin.conditionalRegistration("i18n", "i18nText");

        // handle rendering of the freemarker template.
        StringWriter writer = new StringWriter();

        Template template = config.getTemplate("inline-form.ftl");
        template.process(context, writer);

        writer.append("\n===================\n");

        template = config.getTemplate("form.ftl");
        template.process(context, writer);

        System.out.println(writer.toString());
    }

    private static TemplateLoader getMultiLoader()
    {
        File root = new File("c:/projects/pulse/trunk");
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
