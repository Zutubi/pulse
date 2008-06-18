package com.zutubi.prototype.webwork;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.prototype.actions.ActionManager;
import com.zutubi.prototype.actions.ConfigurationAction;
import com.zutubi.prototype.actions.ConfigurationActions;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.util.TextUtils;
import com.zutubi.util.logging.Logger;

import java.util.List;

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
        if(!TextUtils.stringSet(path))
        {
            addActionError("Path is required");
            return ERROR;
        }

        if(!TextUtils.stringSet(actionName))
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
            if(TextUtils.stringSet(customAction))
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
            record = PrototypeUtils.toRecord(argumentType, ActionContext.getContext().getParameters());

            String parentPath = PathUtils.getParentPath(path);
            String baseName = PathUtils.getBaseName(path);
            try
            {
                argument = configurationTemplateManager.validate(parentPath, baseName, record, true, false);
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
                return ERROR;
            }
        }

        // All clear to execute action.
        List<String> invalidatedPaths = actionManager.execute(actionName, config, argument);

        if (TextUtils.stringSet(newPath))
        {
            response = new ConfigurationResponse(newPath, null);
        }
        else
        {
            response = new ConfigurationResponse(path, configurationTemplateManager.getTemplatePath(path));
        }

        if(invalidatedPaths != null)
        {
            for(String invalidatedPath: invalidatedPaths)
            {
                response.addRenamedPath(new ConfigurationResponse.Rename(invalidatedPath, invalidatedPath, PrototypeUtils.getDisplayName(invalidatedPath, configurationTemplateManager)));
            }
        }
        
        return SUCCESS;
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }
}
