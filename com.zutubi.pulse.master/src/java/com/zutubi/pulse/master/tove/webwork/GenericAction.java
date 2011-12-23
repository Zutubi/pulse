package com.zutubi.pulse.master.tove.webwork;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.i18n.Messages;
import com.zutubi.tove.ConventionSupport;
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

import java.util.Map;

/**
 * Executes configuration actions on instances, where the action name is passed
 * in as part of the request.
 */
public class GenericAction extends ToveActionSupport
{
    private static final Messages I18N = Messages.getInstance(GenericAction.class);
    private static final Logger LOG = Logger.getLogger(GenericAction.class);

    private ActionManager actionManager;

    /**
     * The action that should be executed.
     */
    private String actionName;
    /**
     * If set to true, execute the action on all concrete descendants for which
     * it is enabled.
     */
    private boolean descendants = false;
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

    public void setDescendants(boolean descendants)
    {
        this.descendants = descendants;
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
        newPanel = new ConfigurationPanel("ajax/config/action.vm");
    }

    public void doCancel()
    {
        if(isParentEmbeddedCollection())
        {
            path = PathUtils.getParentPath(path);
        }

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

        if (descendants)
        {
            return executeDescendantsAction(config);
        }
        else
        {
            return executeSingleAction(config);
        }
    }

    private String executeDescendantsAction(Configuration config)
    {
        String templateOwnerPath = configurationTemplateManager.getTemplateOwnerPath(path);
        if (templateOwnerPath == null)
        {
            addActionError("Requested descendants action for path '" + path + "' not in a templated scope");
            return ERROR;
        }

        response = new ConfigurationResponse(path, configurationTemplateManager.getTemplatePath(path));
        Map<String, ActionResult> results = actionManager.executeOnDescendants(actionName, path);
        int failureCount = 0;
        for (ActionResult result: results.values())
        {
            if (result.getStatus() == ActionResult.Status.FAILURE)
            {
                failureCount++;
            }

            recordInvalidatedPaths(result);
        }

        Messages messages = Messages.getInstance(config.getClass());
        String key = actionName + ConventionSupport.I18N_KEY_SUFFIX_LABEL;
        String actionLabel = messages.isKeyDefined(key) ? messages.format(key) : actionName;

        if (failureCount == 0)
        {
            String messageKey = "descendants.triggered." + (results.size() == 1 ? "single" : "multiple");
            response.setStatus(new ConfigurationResponse.Status(ConfigurationResponse.Status.Type.SUCCESS, I18N.format(messageKey, actionLabel, results.size())));
        }
        else
        {
            response.setStatus(new ConfigurationResponse.Status(ConfigurationResponse.Status.Type.FAILURE, I18N.format("descendants.failed", actionLabel, failureCount, results.size())));
        }

        return SUCCESS;
    }

    private String executeSingleAction(Configuration config)
    {
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
                    record = type.unstantiate(result, null);
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
        ActionResult actionResult;
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

        recordInvalidatedPaths(actionResult);

        return SUCCESS;
    }

    private void recordInvalidatedPaths(ActionResult actionResult)
    {
        for(String invalidatedPath: actionResult.getInvalidatedPaths())
        {
            String newDisplayName = ToveUtils.getDisplayName(invalidatedPath, configurationTemplateManager);
            String collapsedCollection = ToveUtils.getCollapsedCollection(invalidatedPath, configurationTemplateManager.getType(invalidatedPath), configurationSecurityManager);
            response.addRenamedPath(new ConfigurationResponse.Rename(invalidatedPath, invalidatedPath, newDisplayName, collapsedCollection));
        }
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
