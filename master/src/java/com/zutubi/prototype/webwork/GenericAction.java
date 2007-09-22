package com.zutubi.prototype.webwork;

import com.opensymphony.util.TextUtils;
import com.zutubi.prototype.actions.ActionManager;
import com.zutubi.prototype.type.CompositeType;

/**
 */
public class GenericAction extends PrototypeSupport
{
    private ActionManager actionManager;

    /**
     * The action that should be executed.
     */
    private String action;
    private String newPath;

    public void setAction(String action)
    {
        this.action = action;
    }

    public void setNewPath(String newPath)
    {
        this.newPath = newPath;
    }

    public String execute() throws Exception
    {
        CompositeType type = (CompositeType) configurationTemplateManager.getType(path);

        // need the configuration instance.
        Object config = configurationTemplateManager.getInstance(path);
        actionManager.execute(action, config);

        doRender();

        // FIXME: want to trigger a reload of the same page, not necessarily always a reload of the configs path, since
        // the action may be triggered from multiple locations.
        if (TextUtils.stringSet(newPath))
        {
            response = new ConfigurationResponse(newPath, null);
        }
        else
        {
            response = new ConfigurationResponse(path, configurationTemplateManager.getTemplatePath(path));
        }

        return SUCCESS;
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }
}
