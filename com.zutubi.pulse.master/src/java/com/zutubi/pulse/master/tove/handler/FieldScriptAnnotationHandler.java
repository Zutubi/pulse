package com.zutubi.pulse.master.tove.handler;

import com.zutubi.pulse.master.bootstrap.freemarker.FreemarkerConfigurationFactoryBean;
import com.zutubi.pulse.master.rest.model.forms.FieldModel;
import com.zutubi.tove.annotations.FieldScript;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.util.StringUtils;
import com.zutubi.util.SystemUtils;
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

    static void loadTemplate(Class clazz, FieldModel field, String templateName, Configuration freemarkerConfiguration) throws IOException, TemplateException
    {
        if (StringUtils.stringSet(templateName))
        {
            Map<String, Object> freemarkerContext = new HashMap<>();
            freemarkerContext.put("field", field);
            freemarkerContext.put("isWindows", Boolean.toString(SystemUtils.IS_WINDOWS));

            freemarker.template.Configuration config = FreemarkerConfigurationFactoryBean.addClassTemplateLoader(clazz, freemarkerConfiguration);
            Template template = config.getTemplate(templateName + ".ftl");
            StringWriter out = new StringWriter();
            template.process(freemarkerContext, out);
            field.addScript(out.toString());
        }
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
