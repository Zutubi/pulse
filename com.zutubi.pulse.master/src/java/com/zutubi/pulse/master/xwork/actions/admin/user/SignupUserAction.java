package com.zutubi.pulse.master.xwork.actions.admin.user;

import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.security.AcegiUtils;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.user.SignupUserConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.pulse.master.tove.webwork.TransientAction;
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
        user = userManager.insert(user);

        setPassword(user, instance.getPassword());

        User state = userManager.getUser(instance.getLogin());
        state.setConfig(user);
        AcegiUtils.loginAs(userManager.getPrinciple(state));
        return SUCCESS;
    }

    private void setPassword(final UserConfiguration user, final String password)
    {
        AcegiUtils.runAsSystem(new Runnable()
        {
            public void run()
            {
                userManager.setPassword(user, password);
            }
        });
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
