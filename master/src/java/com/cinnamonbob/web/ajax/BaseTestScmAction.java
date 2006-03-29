package com.cinnamonbob.web.ajax;

import com.cinnamonbob.model.Scm;
import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.scm.SCMServer;
import com.cinnamonbob.web.ActionSupport;
import com.opensymphony.xwork.validator.DefaultActionValidatorManager;
import com.opensymphony.xwork.validator.DelegatingValidatorContext;
import com.opensymphony.xwork.validator.ValidationException;

/**
 * <class-comment/>
 */
public abstract class BaseTestScmAction extends ActionSupport
{
    private DefaultActionValidatorManager validationManager = new DefaultActionValidatorManager();

    /**
     * The scm to be tested.
     *
     * @return a configured scm instance.
     */
    public abstract Scm getScm();

    /**
     * Run the scm test.
     *
     * @return SUCCESS. The action here is to test the connection, so regardless of the validation state of the
     * connection, we always successfully test.
     */
    public String execute()
    {
        Scm scm = getScm();
        try
        {
            // validate the scm.
            validationManager.validate(scm, scm.getClass().getName(), new DelegatingValidatorContext(this, this, this));
        }
        catch (ValidationException e)
        {
            // noop.
        }

        if (hasErrors())
        {
            // We are just testing, we always succeed in testing, even if the
            // result is a test failure!
            return SUCCESS;
        }

        try
        {
            SCMServer server = scm.createServer();
            server.testConnection();
        }
        catch (SCMException e)
        {
            addActionError(e.getMessage());
        }

        return SUCCESS;
    }

}
