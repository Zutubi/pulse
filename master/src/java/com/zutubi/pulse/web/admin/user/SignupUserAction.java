package com.zutubi.pulse.web.admin.user;

import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.webwork.TransientAction;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.prototype.config.admin.GeneralAdminConfiguration;
import com.zutubi.pulse.prototype.config.user.SignupUserConfiguration;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;
import com.zutubi.pulse.security.AcegiUtils;
import org.acegisecurity.AccessDeniedException;

/**
 */
public class SignupUserAction extends TransientAction<SignupUserConfiguration>
{
    private ConfigurationProvider configurationProvider;

    public SignupUserAction()
    {
        super("transient/signup");
    }

    protected SignupUserConfiguration initialise() throws Exception
    {
        return new SignupUserConfiguration();
    }

    protected String complete(SignupUserConfiguration instance) throws Exception
    {
        if (!configurationProvider.get(GeneralAdminConfiguration.class).isAnonymousSignupEnabled())
        {
            throw new AccessDeniedException("Anonymous signup is not enabled");
        }

        UserConfiguration user = new UserConfiguration(instance.getLogin(), instance.getName());
        user.setPassword(instance.getPassword());
        configurationProvider.insert(ConfigurationRegistry.USERS_SCOPE, user);
        User state = userManager.getUser(instance.getLogin());
        state.setConfig(user);
        AcegiUtils.loginAs(userManager.getPrinciple(state));
        return SUCCESS;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
