package com.zutubi.prototype.webwork;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.prototype.ConfigurationCheckHandler;
import com.zutubi.prototype.annotation.ConfigurationCheck;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.validation.XWorkValidationAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Process a configuration check request.
 *
 */
public class CheckAction extends ActionSupport
{
    private String path;

    private TypeRegistry typeRegistry;
    
    private PrototypeInteractionHandler interactionHandler;


    public CheckAction()
    {
        interactionHandler = new PrototypeInteractionHandler();
        ComponentContext.autowire(interactionHandler);
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }

    public String execute() throws Exception
    {
        Map<String, String[]> parameters = ActionContext.getContext().getParameters();

        Map<String, String[]> checkParameters = new HashMap<String, String[]>();
        for (String name : parameters.keySet())
        {
            if (name.endsWith("_check"))
            {
                checkParameters.put(name.substring(0, name.length() - 6), parameters.get(name));
            }
        }

        String symbolicName = checkParameters.get("symbolicName")[0];
        
        Type type = typeRegistry.getType(symbolicName);

        Record record = PrototypeUtils.toRecord((CompositeType) type, checkParameters);
        if (!interactionHandler.validate(record, new XWorkValidationAdapter(this)))
        {
            return SUCCESS;
        }

        Object instance = type.instantiate(record);

        ConfigurationCheck annotation = (ConfigurationCheck) type.getAnnotation(ConfigurationCheck.class);
        type = typeRegistry.getType(annotation.value());
        if (type == null)
        {
            type = typeRegistry.register(annotation.value());
        }

        record = PrototypeUtils.toRecord((CompositeType) type, parameters);
        if (!interactionHandler.validate(record, new XWorkValidationAdapter(this)))
        {
            return SUCCESS;
        }

        ConfigurationCheckHandler handler = (ConfigurationCheckHandler) type.instantiate(record);
        handler.test(instance);

        // We need to return the existing form values as well.

        return SUCCESS;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
