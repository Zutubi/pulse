package com.zutubi.prototype.webwork;

import com.zutubi.prototype.ConventionSupport;
import com.zutubi.prototype.actions.Actions;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.util.bean.ObjectFactory;

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

    /**
     * Setter for the action property
     *
     * @param action identifies the action to be executed
     */
    public void setAction(String action)
    {
        this.action = action;
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

        response = new ConfigurationResponse(path, configurationTemplateManager.getTemplatePath(path));

        return SUCCESS;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
