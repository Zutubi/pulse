package com.zutubi.prototype.webwork;

import com.zutubi.prototype.ConventionSupport;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.util.logging.Logger;

import java.lang.reflect.Method;

/**
 *
 *
 */
public class GenericAction extends PrototypeSupport
{
    private static final Logger LOG = Logger.getLogger(GenericAction.class);

    /**
     * The action that should be executed.
     */
    private String action;

    public void setAction(String action)
    {
        this.action = action;
    }

    public String execute() throws Exception
    {
        CompositeType type = (CompositeType) configurationTemplateManager.getType(path);

        // need the action handler.
        Class handlerClass = ConventionSupport.getActions(type);
        Object actionHandler = handlerClass.newInstance();

        ComponentContext.autowire(actionHandler);

        // need the configuration instance.
        Object config = configurationTemplateManager.getInstance(path);

        Method actionMethod = ConventionSupport.getActionMethod(handlerClass, type, action);
        actionMethod.invoke(actionHandler, config);

        doRender();

        return SUCCESS;
    }

    private Object getInfo(CompositeType type)
    {
        Class clazz = type.getClazz();
        try
        {
            String infoClassName = clazz.getCanonicalName() + "Info";
            Class infoClazz = clazz.getClassLoader().loadClass(infoClassName);
            return infoClazz.newInstance();
        }
        catch (Exception e)
        {
            LOG.debug(e);
        }
        return null;
    }
}
