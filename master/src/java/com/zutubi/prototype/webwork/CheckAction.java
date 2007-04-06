package com.zutubi.prototype.webwork;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.prototype.ConfigurationCheckHandler;
import com.zutubi.prototype.annotation.ConfigurationCheck;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
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
    private PrototypeInteractionHandler interactionHandler;

    private Record checkRecord;

    public CheckAction()
    {
        interactionHandler = new PrototypeInteractionHandler();
        ComponentContext.autowire(interactionHandler);
    }

    public Record getCheckRecord()
    {
        return checkRecord;
    }

    public String execute() throws Exception
    {
        if (!isCheckSelected())
        {
            return ERROR;
        }

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
        ConfigurationCheck annotation = (ConfigurationCheck) type.getAnnotation(ConfigurationCheck.class);
        Type checkType = typeRegistry.getType(annotation.value());
        if (checkType == null)
        {
            checkType = typeRegistry.register(annotation.value());
        }
        checkRecord = PrototypeUtils.toRecord((CompositeType) checkType, parameters);

        // validate the check form first.
        if (!interactionHandler.validate(checkRecord, new XWorkValidationAdapter(this)))
        {
            return doRender();
        }

        // validate the primary form.
        if (!interactionHandler.validate(record, new XWorkValidationAdapter(this)))
        {
            return doRender();
        }

        // Instantiate the primary configuration object.
        Object instance = type.instantiate(record);

        // Instantiate and execute the check handler.
        ConfigurationCheckHandler handler = (ConfigurationCheckHandler) checkType.instantiate(checkRecord);
        handler.test(instance);

        // We need to return the existing form values as well.

        return doRender();
    }
}
