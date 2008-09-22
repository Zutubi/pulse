package com.zutubi.pulse.web.admin.user;

import com.zutubi.pulse.model.User;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.pulse.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.tove.config.user.SignupUserConfiguration;
import com.zutubi.pulse.tove.config.user.UserConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.webwork.TransientAction;
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
        if (!configurationProvider.get(GlobalConfiguration.class).isAnonymousSignupEnabled())
        {
            throw new AccessDeniedException("Anonymous signup is not enabled");
        }

        UserConfiguration user = new UserConfiguration(instance.getLogin(), instance.getName());
        user.setPassword(instance.getPassword());
        user = userManager.insert(user);

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
