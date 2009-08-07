package com.zutubi.pulse.master.tove.webwork;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.actions.ConfigurationAction;
import com.zutubi.tove.actions.ConfigurationActions;
import com.zutubi.tove.config.api.ActionResult;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;

/**
 */
public class GenericAction extends ToveActionSupport
{
    private static final Logger LOG = Logger.getLogger(GenericAction.class);

    private ActionManager actionManager;

    /**
     * The action that should be executed.
     */
    private String actionName;
    private String customAction;
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

    public String getCustomAction()
    {
        return customAction;
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

    public void doCancel()
    {
        response = new ConfigurationResponse(path, configurationTemplateManager.getTemplatePath(path));
    }

    public String execute() throws Exception
    {
        if(!StringUtils.stringSet(path))
        {
            addActionError("Path is required");
            return ERROR;
        }

        if(!StringUtils.stringSet(actionName))
        {
            addActionError("Action name is required");
            return ERROR;
        }

        Configuration config = configurationProvider.get(path, Configuration.class);
        if(config == null)
        {
            addActionError("Path '" + path + "' does not exist");
            return ERROR;
        }

        configurationType = (CompositeType) configurationTemplateManager.getType(path);
        ConfigurationActions configurationActions = actionManager.getConfigurationActions(configurationType);
        ConfigurationAction configurationAction = configurationActions.getAction(actionName);

        if(configurationAction == null)
        {
            addActionError("Unknown action '" + actionName + "' for path '" + path + "'");
            return ERROR;
        }

        if (isInputSelected())
        {
            customAction = actionManager.getCustomiseName(actionName, config);
            if(StringUtils.stringSet(customAction))
            {
                return "chain";
            }

            Configuration result = actionManager.prepare(actionName, config);
            if(result != null)
            {
                CompositeType type = typeRegistry.getType(result.getClass());
                try
                {
                    record = type.unstantiate(result);
                }
                catch (TypeException e)
                {
                    LOG.severe("Unable to unstantiate prepared argument: " + e.getMessage(), e);
                }
            }
        }

        if (configurationAction.hasArgument())
        {
            type = typeRegistry.getType(configurationAction.getArgumentClass());

            if (isInputSelected())
            {
                prepare();
                return INPUT;
            }
        }

        // If we have an argument, it must be valid.
        Configuration argument = null;
        if (configurationAction.hasArgument())
        {
            // Instantiate the argument from POSTed params.
            CompositeType argumentType = typeRegistry.getType(configurationAction.getArgumentClass());
            record = ToveUtils.toRecord(argumentType, ActionContext.getContext().getParameters());

            String parentPath = PathUtils.getParentPath(path);
            String baseName = PathUtils.getBaseName(path);
            try
            {
                argument = configurationTemplateManager.validate(parentPath, baseName, record, true, false);
                if (!argument.isValid())
                {
                    ToveUtils.mapErrors(argument, this, null);
                }
            }
            catch (TypeException e)
            {
                addActionError(e.getMessage());
            }

            if (hasErrors())
            {
                return ERROR;
            }
        }

        // All clear to execute action.
        ActionResult actionResult = null;
        try
        {
            actionResult = actionManager.execute(actionName, config, argument);
        }
        catch (Exception e)
        {
            actionResult = new ActionResult(ActionResult.Status.FAILURE, e.getMessage());
        }

        if (StringUtils.stringSet(newPath))
        {
            response = new ConfigurationResponse(newPath, null);
        }
        else
        {
            response = new ConfigurationResponse(path, configurationTemplateManager.getTemplatePath(path));
        }

        if (StringUtils.stringSet(actionResult.getMessage()))
        {
            response.setStatus(new ConfigurationResponse.Status(mapStatus(actionResult.getStatus()), actionResult.getMessage()));
        }

        for(String invalidatedPath: actionResult.getInvalidatedPaths())
        {
            String newDisplayName = ToveUtils.getDisplayName(invalidatedPath, configurationTemplateManager);
            String collapsedCollection = ToveUtils.getCollapsedCollection(invalidatedPath, configurationTemplateManager.getType(invalidatedPath), configurationSecurityManager);
            response.addRenamedPath(new ConfigurationResponse.Rename(invalidatedPath, invalidatedPath, newDisplayName, collapsedCollection));
        }
        
        return SUCCESS;
    }

    private ConfigurationResponse.Status.Type mapStatus(ActionResult.Status status)
    {
        switch(status)
        {
            case SUCCESS:
                return ConfigurationResponse.Status.Type.SUCCESS;
            case FAILURE:
                return ConfigurationResponse.Status.Type.FAILURE;
            default:
                throw new RuntimeException("Unrecognised action status '" + status + "'");
        }
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }
}
