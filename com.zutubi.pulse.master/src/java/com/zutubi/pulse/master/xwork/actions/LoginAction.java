package com.zutubi.pulse.master.xwork.actions;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.master.tove.config.misc.LoginConfiguration;
import com.zutubi.pulse.master.tove.webwork.TransientAction;
import com.zutubi.util.logging.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;

import java.util.Map;

/**
 * This login action is to provide mapping support between how Acegi expects the
 * data and how webwork likes to present it.
 *
 * 1) Authentication errors are available via the WebAttributes.AUTHENTICATION_EXCEPTION
 *    session variable.
 * 2) j_username and j_password are the credentials expected by acegi.
 * 3) If an authentication error occurs, the error string is set to true.
 */
public class LoginAction extends TransientAction<LoginConfiguration>
{
    private static final Logger LOG = Logger.getLogger(LoginAction.class);

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
        Map session = ActionContext.getContext().getSession();
        if (authenticationError)
        {
            AuthenticationException ae = (AuthenticationException) session.get(WebAttributes.AUTHENTICATION_EXCEPTION);
            if (ae != null)
            {
                String username = (String)ae.getAuthentication().getPrincipal();
                LOG.info("Authentication failure: '" + username + "': " + ae.getMessage());
                result.setJ_username(username);
            }

            addActionError(getText("login.badcredentials"));
        }

        return result;
    }

    protected String complete(LoginConfiguration instance) throws Exception
    {
        return SUCCESS;
    }
}
