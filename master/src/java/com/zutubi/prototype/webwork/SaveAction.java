package com.zutubi.prototype.webwork;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.validation.XWorkValidationAdapter;

/**
 *
 *
 */
public class SaveAction extends PrototypeSupport
{
    private String symbolicName;

    private PrototypeInteractionHandler interactionHandler;

    public SaveAction()
    {
        interactionHandler = new PrototypeInteractionHandler();
        ComponentContext.autowire(interactionHandler);
    }

    public String getSymbolicName()
    {
        return symbolicName;
    }

    public void setSymbolicName(String symbolicName)
    {
        this.symbolicName = symbolicName;
    }

    public String execute() throws Exception
    {
        if (isSaveSelected())
        {
            return doSave();
        }
        return doRender();
    }

    private String doSave() throws Exception
    {
        if (!TextUtils.stringSet(symbolicName))
        {
            return doRender();
        }

        CompositeType type = typeRegistry.getType(symbolicName);
        if (type == null)
        {
            return doRender();
        }

        record = PrototypeUtils.toRecord(type, ActionContext.getContext().getParameters());

        if (!interactionHandler.validate(record, new XWorkValidationAdapter(this)))
        {
            return doRender();
        }

        interactionHandler.save(path, record);

        return doRender();
    }
}
