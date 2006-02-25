package com.cinnamonbob.web;

import com.opensymphony.xwork.ActionContext;
import com.cinnamonbob.util.logging.Logger;

import java.util.Map;

import org.acegisecurity.ui.AbstractProcessingFilter;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.userdetails.User;

/**
 * This login action is to provide mapping support between how Acegi expects the
 * data and how webwork likes to present it.
 *
 * 1) Authentication errors are available via the AbstractProcessingFilter.ACEGI_SECURITY_LAST_EXCEPTION_KEY
 *    session variable.
 * 2) j_username and j_password are the credentials expected by acegi.
 * 3) If an authentication error occurs, the error string is set to true.
 */
public class LoginAction extends ActionSupport
{
    private static final Logger LOG = Logger.getLogger(LoginAction.class);

    private boolean authenticationError = false;
    private String username;
    private String password;
    private String rememberMe;

    public void setError(boolean error)
    {
        this.authenticationError = error;
    }

    public String getJ_password()
    {
        return password;
    }

    public void setJ_password(String password)
    {
        this.password = password;
    }

    public String getJ_username()
    {
        return username;
    }

    public void setJ_username(String username)
    {
        this.username = username;
    }

    public String get_acegi_security_remember_me()
    {
        return rememberMe;
    }

    public void set_acegi_security_remember_me(String rememberMe)
    {
        this.rememberMe = rememberMe;
    }

    public void validate()
    {
        Map session = ActionContext.getContext().getSession();
        if (authenticationError)
        {
            AuthenticationException ae = (AuthenticationException) session.get(AbstractProcessingFilter.ACEGI_SECURITY_LAST_EXCEPTION_KEY);
            username = (String)ae.getAuthentication().getPrincipal();
            addActionError(ae.getMessage());
        }
    }

    public String execute()
    {
        if (authenticationError)
        {
            return INPUT;
        }
        return SUCCESS;
    }
}
