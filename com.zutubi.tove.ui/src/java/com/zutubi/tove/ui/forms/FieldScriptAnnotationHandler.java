package com.zutubi.tove.ui.forms;

import com.zutubi.tove.annotations.FieldScript;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.model.forms.FieldModel;
import com.zutubi.util.StringUtils;
import com.zutubi.util.SystemUtils;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler for the {@link @com.zutubi.tove.annotations.FieldScript} annotation.
 */
public class FieldScriptAnnotationHandler implements AnnotationHandler
{
    private Configuration freemarkerConfiguration;

    public static void loadTemplate(Class clazz, FieldModel field, String templateName, Configuration freemarkerConfiguration) throws IOException, TemplateException
    {
        if (StringUtils.stringSet(templateName))
        {
            Map<String, Object> freemarkerContext = new HashMap<>();
            freemarkerContext.put("field", field);
            freemarkerContext.put("isWindows", Boolean.toString(SystemUtils.IS_WINDOWS));

            freemarker.template.Configuration config = addClassTemplateLoader(clazz, freemarkerConfiguration);
            Template template = config.getTemplate(templateName + ".ftl");
            StringWriter out = new StringWriter();
            template.process(freemarkerContext, out);
            field.addScript(out.toString());
        }
    }

    private static Configuration addClassTemplateLoader(Class clazz, Configuration configuration)
    {
        Configuration newConfig = (Configuration) configuration.clone();
        TemplateLoader currentLoader = configuration.getTemplateLoader();
        TemplateLoader classLoader = new ClassTemplateLoader(clazz, "");
        MultiTemplateLoader loader = new MultiTemplateLoader(new TemplateLoader[]{ classLoader, currentLoader });
        newConfig.setTemplateLoader(loader);
        return newConfig;
    }

    @Override
    public boolean requiresContext(Annotation annotation)
    {
        return false;
    }

    @Override
    public void process(CompositeType annotatedType, TypeProperty property, Annotation annotation, FieldModel field, FormContext context) throws Exception
    {
        FieldScript fieldScript = (FieldScript) annotation;
        String template = fieldScript.template();
        if(!StringUtils.stringSet(template))
        {
            template = annotatedType.getClazz().getSimpleName() + "." + field.getName();
        }

        loadTemplate(annotatedType.getClazz(), field, template, freemarkerConfiguration);
    }

    public void setFreemarkerConfiguration(Configuration freemarkerConfiguration)
    {
        this.freemarkerConfiguration = freemarkerConfiguration;
    }
}
