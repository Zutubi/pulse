package com.zutubi.prototype.webwork;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.prototype.ConfigurationCheckHandler;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.validation.XWorkValidationAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Process a configuration check request.
 *
 */
public class CheckAction extends PrototypeSupport
{
    private Record checkRecord;
    private ConfigurationErrors configurationErrors = new ConfigurationErrors(this);
    private CheckResponse checkResponse;
    private freemarker.template.Configuration freemarkerConfiguration;

    public Record getCheckRecord()
    {
        return checkRecord;
    }

    public ConfigurationErrors getConfigurationErrors()
    {
        return configurationErrors;
    }

    public CheckResponse getCheckResponse()
    {
        return checkResponse;
    }

    public String execute() throws Exception
    {
        // first, gather all of the parameters (both those being checked and those used by the checking) so that
        // we have something to display back to the user.

        Map<String, String[]> parameters = ActionContext.getContext().getParameters();

        // The form being processed:
        Map<String, String[]> formParameters = new HashMap<String, String[]>();
        for (String name : parameters.keySet())
        {
            if (name.endsWith("_check"))
            {
                formParameters.put(name.substring(0, name.length() - 6), parameters.get(name));
            }
        }

        String symbolicName = formParameters.get("symbolicName")[0];
        Type type = typeRegistry.getType(symbolicName);
        record = PrototypeUtils.toRecord((CompositeType) type, formParameters);

        // Now lets create the record for the secondary form, used to generate the check processor. 
        CompositeType checkType = configurationRegistry.getConfigurationCheckType((CompositeType) type);
        checkRecord = PrototypeUtils.toRecord(checkType, parameters);

        // validate the check form first.
        boolean valid = true;
        if (!configurationPersistenceManager.validate(null, null, checkRecord, new XWorkValidationAdapter(this)))
        {
            valid = false;
        }

        // validate the primary form.
        if (!configurationPersistenceManager.validate(PathUtils.getParentPath(path), PathUtils.getBaseName(path), record, new XWorkValidationAdapter(this, "_check")))
        {
            valid = false;
        }

        if (!valid)
        {
            return INPUT;
        }

        // Instantiate the primary configuration object.
        Object instance = type.instantiate(null, record);

        // Instantiate and execute the check handler.
        ConfigurationCheckHandler handler = (ConfigurationCheckHandler) checkType.instantiate(null, checkRecord);
        ComponentContext.autowire(handler);
        Exception exception = null;
        try
        {
            handler.test(instance);
        }
        catch (Exception e)
        {
            exception = e;
        }
        
        checkResponse = new CheckResponse(instance, handler, exception, freemarkerConfiguration);
        return SUCCESS;
    }

    public void setFreemarkerConfiguration(freemarker.template.Configuration freemarkerConfiguration)
    {
        this.freemarkerConfiguration = freemarkerConfiguration;
    }
}
