package com.zutubi.pulse.master.tove.webwork;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.tove.config.ConfigurationReferenceManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.api.ConfigurationCheckHandler;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.SimpleInstantiator;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;

import java.util.HashMap;
import java.util.Map;

/**
 * Process a configuration check request.
 *
 */
public class CheckAction extends ToveActionSupport
{
    private Record checkRecord;
    private ConfigurationErrors configurationErrors = new ConfigurationErrors(this);
    private CheckResponse checkResponse;
    private freemarker.template.Configuration freemarkerConfiguration;
    private ConfigurationReferenceManager configurationReferenceManager;

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

        @SuppressWarnings({"unchecked"})
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
        CompositeType type = typeRegistry.getType(symbolicName);
        record = ToveUtils.toRecord(type, formParameters);
        
        if (configurationTemplateManager.isPersistent(path))
        {
            Record existingRecord = configurationTemplateManager.getRecord(path);
            if (existingRecord != null)
            {
                ToveUtils.unsuppressPasswords(existingRecord, (MutableRecord) record, type, false);
            }
        }

        // Now lets create the record for the secondary form, used to generate the check processor. 
        CompositeType checkType = configurationRegistry.getConfigurationCheckType((CompositeType) type);
        checkRecord = ToveUtils.toRecord(checkType, parameters);

        try
        {
            String parentPath = PathUtils.getParentPath(path);
            String baseName = PathUtils.getBaseName(path);
            Configuration checkInstance = configurationTemplateManager.validate(parentPath, baseName, checkRecord, true, false);
            Configuration mainInstance = configurationTemplateManager.validate(parentPath, baseName, record, true, false);
            if (!checkInstance.isValid() || !mainInstance.isValid())
            {
                ToveUtils.mapErrors(checkInstance, this, null);
                ToveUtils.mapErrors(mainInstance, this, "_check");
            }
        }
        catch (TypeException e)
        {
            addActionError(e.getMessage());
        }

        if(hasErrors())
        {
            return INPUT;
        }

        // Instantiate the primary configuration object.
        SimpleInstantiator instantiator = new SimpleInstantiator(configurationTemplateManager.getTemplateOwnerPath(path), configurationReferenceManager, configurationTemplateManager);
        Configuration instance = (Configuration) instantiator.instantiate(type, record);
        instance.setConfigurationPath(path);
        
        // Instantiate and execute the check handler.
        ConfigurationCheckHandler handler = (ConfigurationCheckHandler) instantiator.instantiate(checkType, checkRecord);
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

    public void setConfigurationReferenceManager(ConfigurationReferenceManager configurationReferenceManager)
    {
        this.configurationReferenceManager = configurationReferenceManager;
    }
}
