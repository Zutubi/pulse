package com.zutubi.prototype.webwork;

import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.table.ActionDefinition;
import com.zutubi.util.logging.Logger;
import com.zutubi.pulse.bootstrap.ComponentContext;

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
        CompositeType type = (CompositeType) configurationPersistenceManager.getType(path);

        // need the action handler.
        Object info = getInfo(type);

        Class handlerClass = ((ActionDefinition) info).getActionHandler();
        Object actionHandler = handlerClass.newInstance();

        ComponentContext.autowire(actionHandler);

        Method actionMethod = handlerClass.getMethod("do" + action, type.getClazz());

        // need the configuration instance.
        Object config = configurationPersistenceManager.getInstance(path);

        // put the two together.
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
