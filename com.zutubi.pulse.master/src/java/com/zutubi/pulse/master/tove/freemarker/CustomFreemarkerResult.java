package com.zutubi.pulse.master.tove.freemarker;

import com.opensymphony.webwork.views.freemarker.FreemarkerResult;
import com.opensymphony.xwork.ActionInvocation;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.pulse.master.bootstrap.freemarker.FreemarkerConfigurationFactoryBean;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import java.io.IOException;

/**
 * This is a very specific extension of the default Freemarker Result that loads
 * a freemarker template based on the name of the type being rendered, from the
 * classpath of the type being rendered.
 *
 * Specifically, this allows for the default rendering of types in the tove UI to
 * be overridden.
 */
public class CustomFreemarkerResult extends FreemarkerResult
{
    protected Configuration getConfiguration() throws TemplateException
    {
        CompositeType type = (CompositeType) this.invocation.getStack().findValue("type", CompositeType.class);
        return FreemarkerConfigurationFactoryBean.addClassTemplateLoader(type.getClazz(), super.getConfiguration());
    }

    public void doExecute(String location, ActionInvocation invocation) throws IOException, TemplateException
    {
        super.doExecute(location, invocation);
    }
}
