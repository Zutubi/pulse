package com.zutubi.pulse.web.ajax;

import com.opensymphony.xwork.validator.DefaultActionValidatorManager;
import com.opensymphony.xwork.validator.DelegatingValidatorContext;
import com.opensymphony.xwork.validator.ValidationException;
import com.zutubi.pulse.model.Scm;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.SCMClient;
import com.zutubi.pulse.web.ActionSupport;

import java.util.concurrent.*;

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

        FutureTask<String> task = new FutureTask<String>(new Tester(scm));
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(task);

        try
        {
            String error = task.get(30, TimeUnit.SECONDS);
            if(error != null)
            {
                addActionError(error);
            }
        }
        catch (TimeoutException e)
        {
            addActionError("Connection test timed out (after 30 seconds)");
        }
        catch (Exception e)
        {
            addActionError(e.getMessage());
        }

        return SUCCESS;
    }

    private class Tester implements Callable<String>
    {
        private Scm scm;

        public Tester(Scm scm)
        {
            this.scm = scm;
        }

        public String call() throws Exception
        {
            try
            {
                SCMClient client = scm.createServer();
                client.testConnection();
                return null;
            }
            catch (SCMException e)
            {
                return e.getMessage();
            }
        }
    }
}
