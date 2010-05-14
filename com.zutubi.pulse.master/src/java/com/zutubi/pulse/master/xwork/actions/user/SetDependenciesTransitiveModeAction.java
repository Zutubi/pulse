package com.zutubi.pulse.master.xwork.actions.user;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.master.dependency.ProjectDependencyGraphBuilder;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.ajax.SimpleResult;
import com.zutubi.pulse.master.xwork.actions.project.ProjectDependenciesAction;
import com.zutubi.tove.config.ConfigurationProvider;

/**
 * Action to set the transitive mode for a user's viewing of dependency graphs.
 */
public class SetDependenciesTransitiveModeAction extends ActionSupport
{
    private String mode;
    private SimpleResult result;
    private ConfigurationProvider configurationProvider;

    public String getMode()
    {
        return mode;
    }

    public void setMode(String mode)
    {
        this.mode = mode;
    }

    public SimpleResult getResult()
    {
        return result;
    }

    @Override @SuppressWarnings({"unchecked"})
    public String execute() throws Exception
    {
        ProjectDependencyGraphBuilder.TransitiveMode transitiveMode = ProjectDependencyGraphBuilder.TransitiveMode.valueOf(mode);

        User user = getLoggedInUser();
        if (user == null)
        {
            // For anonymous users we store this preference in the session.
            ActionContext.getContext().getSession().put(ProjectDependenciesAction.ANONYMOUS_MODE_KEY, transitiveMode);
        }
        else
        {
            UserPreferencesConfiguration preferences = configurationProvider.deepClone(user.getPreferences());
            preferences.setDependencyTransitiveMode(transitiveMode);
            configurationProvider.save(preferences);
        }

        result = new SimpleResult(true, null);
        return SUCCESS;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
