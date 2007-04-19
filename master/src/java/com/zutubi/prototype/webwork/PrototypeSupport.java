package com.zutubi.prototype.webwork;

import com.opensymphony.util.TextUtils;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.i18n.MessagesProvider;
import com.zutubi.i18n.Messages;

/**
 *
 *
 */
public class PrototypeSupport extends ActionSupport implements MessagesProvider
{
    protected String path;

    protected Configuration configuration;

    protected TypeRegistry typeRegistry;
    protected ConfigurationPersistenceManager configurationPersistenceManager;

    protected Record record;
    
    private Type type;

    private String previous;
    private String next;
    private String finish;
    private String save;
    private String check;
    private String confirm;
    private String delete;
    private String submitField;

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

    public void setCheck(String check)
    {
        this.check = check;
    }

    public boolean isCheckSelected()
    {
        if (TextUtils.stringSet(submitField))
        {
            return submitField.equals("check");
        }
        else
        {
            return TextUtils.stringSet(check);
        }
    }

    public void setConfirm(String confirm)
    {
        this.confirm = confirm;
    }

    public boolean isConfirmSelected()
    {
        if (TextUtils.stringSet(submitField))
        {
            return submitField.equals("confirm");
        }
        else
        {
            return TextUtils.stringSet(confirm);
        }
    }

    public void setDelete(String delete)
    {
        this.delete = delete;
    }

    public boolean isDeleteSelected()
    {
        if (TextUtils.stringSet(submitField))
        {
            return submitField.equals("delete");
        }
        else
        {
            return TextUtils.stringSet(delete);
        }
    }

    public void setPrevious(String previous)
    {
        this.previous = previous;
    }

    public boolean isPreviousSelected()
    {
        if (TextUtils.stringSet(submitField))
        {
            return submitField.equals("previous");
        }
        else
        {
            return TextUtils.stringSet(previous);
        }
    }

    public void setNext(String next)
    {
        this.next = next;
    }

    public boolean isNextSelected()
    {
        if (TextUtils.stringSet(submitField))
        {
            return submitField.equals("next");
        }
        else
        {
            return TextUtils.stringSet(next);
        }
    }

    public void setFinish(String finish)
    {
        this.finish = finish;
    }

    public boolean isFinishSelected()
    {
        if (TextUtils.stringSet(submitField))
        {
            return submitField.equals("finish");
        }
        else
        {
            return TextUtils.stringSet(finish);
        }
    }

    public void setSubmitField(String submitField)
    {
        this.submitField = submitField;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public Type getType()
    {
        return type;
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public Record getRecord()
    {
        return record;
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
        if (type instanceof CollectionType)
        {
            return "map";
        }

        // unknown type.
        return ERROR;
    }

    public Messages getMessages()
    {
        return Messages.getInstance(type.getTargetType().getClazz());
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }
}
