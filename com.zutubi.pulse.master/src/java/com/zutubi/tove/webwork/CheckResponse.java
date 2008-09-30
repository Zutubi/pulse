package com.zutubi.tove.webwork;

import com.zutubi.pulse.core.config.ConfigurationCheckHandler;
import com.zutubi.util.logging.Logger;
import freemarker.template.Template;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Response data for a configuration check request, suitable for JSON
 * serialisation.
 */
public class CheckResponse
{
    private static final Logger LOG = Logger.getLogger(CheckResponse.class);

    private Object instance;
    private ConfigurationCheckHandler checkInstance;
    private Exception exception;
    private freemarker.template.Configuration configuration;

    public CheckResponse(Object instance, ConfigurationCheckHandler checkInstance, Exception exception, freemarker.template.Configuration configuration)
    {
        this.instance = instance;
        this.checkInstance = checkInstance;
        this.exception = exception;
        this.configuration = configuration;
    }

    public boolean getSuccess()
    {
        return true;
    }
    
    public boolean getCheckSucceeded()
    {
        return exception == null;
    }

    public String getPanel()
    {
        String templateName;
        if(exception == null)
        {
            templateName = checkInstance.getSuccessTemplate();
        }
        else
        {
            templateName = checkInstance.getFailureTemplate();
        }

        StringWriter writer = new StringWriter();
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("instance", instance);
        context.put("checkInstance", checkInstance);
        context.put("exception", exception);

        try
        {
            Template template = configuration.getTemplate(templateName);
            template.process(context, writer);
            return writer.toString();
        }
        catch (Exception e)
        {
            LOG.severe(e);
            return e.getMessage();
        }
    }
}
