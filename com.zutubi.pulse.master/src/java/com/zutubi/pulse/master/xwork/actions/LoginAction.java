package com.zutubi.pulse.master.xwork.actions;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.master.tove.config.misc.LoginConfiguration;
import com.zutubi.pulse.master.tove.webwork.TransientAction;

/**
 * This login action is to provide mapping support between how Acegi expects the
 * data and how webwork likes to present it.
 */
public class LoginAction extends TransientAction<LoginConfiguration>
{
    private boolean authenticationError = false;

    protected LoginAction()
    {
        super("transient/login", false);
    }

    public void setError(boolean error)
    {
        this.authenticationError = error;
    }

    protected LoginConfiguration initialise() throws Exception
    {
        LoginConfiguration result = new LoginConfiguration();
        if (authenticationError)
        {
            String[] param = (String[]) ActionContext.getContext().getParameters().get("username");
            String username = param == null || param.length == 0 ? "" : param[0];
            result.setUsername(username);
            addActionError(getText("login.badcredentials"));
        }

        return result;
    }

    protected String complete(LoginConfiguration instance) throws Exception
    {
        return SUCCESS;
    }
}
