package com.zutubi.prototype.webwork;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.validation.MessagesTextProvider;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.validation.DelegatingValidationContext;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.ValidationManager;
import com.zutubi.validation.XWorkValidationAdapter;

/**
 *
 *
 */
public class SaveAction extends ActionSupport
{
    private TypeRegistry typeRegistry;
    private ValidationManager validationManager;

    private String symbolicName;
    private String path;

    public String getSymbolicName()
    {
        return symbolicName;
    }

    public void setSymbolicName(String symbolicName)
    {
        this.symbolicName = symbolicName;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String execute() throws Exception
    {
        if (!TextUtils.stringSet(symbolicName))
        {
            return INPUT;
        }

        CompositeType type = typeRegistry.getType(symbolicName);
        if (type == null)
        {
            return ERROR;
        }

        MutableRecord record = PrototypeUtils.toRecord(type, ActionContext.getContext().getParameters());
        if (validate(record))
        {
            return INPUT;
        }

        return SUCCESS;
    }

    private ValidationContext createValidationContext(Object subject)
    {
        MessagesTextProvider textProvider = new MessagesTextProvider(subject);
        return new DelegatingValidationContext(new XWorkValidationAdapter(this), textProvider);
    }

    public boolean validate(Record record)
    {
        try
        {
            Type type = typeRegistry.getType(record.getSymbolicName());
            Object instance = type.instantiate(record);

            ValidationContext context = createValidationContext(instance);

            try
            {
                validationManager.validate(instance, context);
                return !context.hasErrors();
            }
            catch (ValidationException e)
            {
                context.addActionError(e.getMessage());
                return false;
            }
        }
        catch (TypeException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setValidationManager(ValidationManager validationManager)
    {
        this.validationManager = validationManager;
    }
}
