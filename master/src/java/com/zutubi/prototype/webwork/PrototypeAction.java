package com.zutubi.prototype.webwork;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.ListType;
import com.zutubi.prototype.type.MapType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.webwork.Configuration;
import com.zutubi.prototype.webwork.PrototypeUtils;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.validation.XWorkValidationAdapter;

/**
 *
 *
 */
public class PrototypeAction extends ActionSupport
{
    private String path;
    private String symbolicName;
    
    private Configuration configuration;

    private Record record;
    private Type type;

    private String cancel;
    private String save;
    private String submitField;

    private PrototypeInteractionHandler interactionHandler;
    private TypeRegistry typeRegistry;


    public PrototypeAction()
    {
        interactionHandler = new PrototypeInteractionHandler();
        ComponentContext.autowire(interactionHandler);
    }

    public void setCancel(String cancel)
    {
        this.cancel = cancel;
    }

    public boolean isCancelSelected()
    {
        if (TextUtils.stringSet(submitField))
        {
            return submitField.equals("cancel");
        }
        else
        {
            return TextUtils.stringSet(cancel);
        }
    }

    public void setSave(String save)
    {
        this.save = save;
    }

    public boolean isSaveSelected()
    {
        if (TextUtils.stringSet(submitField))
        {
            return submitField.equals("save");
        }
        else
        {
            return TextUtils.stringSet(save);
        }
    }

    public void setSubmitField(String submitField)
    {
        this.submitField = submitField;
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

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public Record getRecord()
    {
        return record;
    }

    public Type getType()
    {
        return type;
    }

    public String execute() throws Exception
    {
        if (isSaveSelected())
        {
            doSave();
        }
        return doRender();
    }

    private void doSave() throws TypeException
    {
        if (!TextUtils.stringSet(symbolicName))
        {
            return;
        }

        CompositeType type = typeRegistry.getType(symbolicName);
        if (type == null)
        {
            return;
        }

        record = PrototypeUtils.toRecord(type, ActionContext.getContext().getParameters());

        if (!validate(record))
        {
            return;
        }

        interactionHandler.save(path, record);
    }

    public boolean validate(Record record) throws TypeException
    {
        return interactionHandler.validate(record, new XWorkValidationAdapter(this));
    }

    public String doRender() throws Exception
    {
        // default handling - render the page.
        configuration = new Configuration(path);
        configuration.analyse();

        if (record == null)
        {
            record = configuration.getRecord();
        }

        // TODO: collapse into a single result vm that handles the various types.
        type = configuration.getType();
        if (type instanceof CompositeType)
        {
            return "composite";
        }
        if (type instanceof ListType)
        {
            return "list";
        }
        if (type instanceof MapType)
        {
            return "map";
        }

        // unknown type.
        return ERROR;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
