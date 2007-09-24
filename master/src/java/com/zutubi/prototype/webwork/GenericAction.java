package com.zutubi.prototype.webwork;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.prototype.actions.ActionManager;
import com.zutubi.prototype.actions.ConfigurationAction;
import com.zutubi.prototype.actions.ConfigurationActions;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.util.logging.Logger;

/**
 */
public class GenericAction extends PrototypeSupport
{
    private static final Logger LOG = Logger.getLogger(GenericAction.class);

    private ActionManager actionManager;

    /**
     * The action that should be executed.
     */
    private String actionName;
    private String newPath;
    private ConfigurationPanel newPanel;
    private CompositeType configurationType;

    public String getActionName()
    {
        return actionName;
    }

    public void setActionName(String actionName)
    {
        this.actionName = actionName;
    }

    public String getNewPath()
    {
        return newPath;
    }

    public void setNewPath(String newPath)
    {
        this.newPath = newPath;
    }

    public ConfigurationPanel getNewPanel()
    {
        return newPanel;
    }

    public CompositeType getConfigurationType()
    {
        return configurationType;
    }

    protected void prepare()
    {
        newPanel = new ConfigurationPanel("aconfig/action.vm");
    }

    public String execute() throws Exception
    {
        if (TextUtils.stringSet(actionName))
        {
            configurationType = (CompositeType) configurationTemplateManager.getType(path);
            ConfigurationActions configurationActions = actionManager.getConfigurationActions(configurationType);
            ConfigurationAction configurationAction = configurationActions.getAction(actionName);

            if (configurationAction != null)
            {
                if (configurationAction.hasArgument())
                {
                    type = typeRegistry.getType(configurationAction.getArgumentClass());

                    if (isInputSelected())
                    {
                        prepare();
                        return INPUT;
                    }
                }

                if (!isCancelSelected())
                {
                    Configuration argument = null;
                    if (configurationAction.hasArgument())
                    {
                        // Instantiate the argument from POSTed params.
                        CompositeType argumentType = typeRegistry.getType(configurationAction.getArgumentClass());
                        record = PrototypeUtils.toRecord(argumentType, ActionContext.getContext().getParameters());

                        String parentPath = PathUtils.getParentPath(path);
                        String baseName = PathUtils.getBaseName(path);
                        try
                        {
                            argument = configurationTemplateManager.validate(parentPath, baseName, record, false);
                            if (!argument.isValid())
                            {
                                PrototypeUtils.mapErrors(argument, this, null);
                            }
                        }
                        catch (TypeException e)
                        {
                            addActionError(e.getMessage());
                        }

                        if (hasErrors())
                        {
                            prepare();
                            return INPUT;
                        }
                    }

                    // need the configuration instance.
                    Configuration config = configurationTemplateManager.getInstance(path);
                    actionManager.execute(actionName, config, argument);
                }

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
            }
            else
            {
                LOG.warning("Request for unknown action '" + actionName + "' on path '" + path + "'");
            }
        }
        else
        {
            LOG.warning("Request for empty action on path '" + path + "'");
        }

        return SUCCESS;
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }
}
