package com.zutubi.prototype.webwork;

import com.zutubi.prototype.ConventionSupport;
import com.zutubi.prototype.actions.Actions;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.util.bean.ObjectFactory;
import com.opensymphony.util.TextUtils;

/**
 *
 *
 */
public class GenericAction extends PrototypeSupport
{
    private ObjectFactory objectFactory;

    /**
     * The action that should be executed.
     */
    private String action;

    private String newPath;

    /**
     * Setter for the action property
     *
     * @param action identifies the action to be executed
     */
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

        // lookup the action handler.
        Class handlerClass = ConventionSupport.getActions(type);
        if (handlerClass == null)
        {
            addActionError("No action handler available for '" + type.getSymbolicName() +"'");
            return ERROR;
        }

        // need the configuration instance.
        Object config = configurationTemplateManager.getInstance(path);

        Actions actions = new Actions();
        actions.setObjectFactory(objectFactory);
        actions.execute(handlerClass, action, config);

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

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
