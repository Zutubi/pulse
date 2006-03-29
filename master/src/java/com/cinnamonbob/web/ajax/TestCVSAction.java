package com.cinnamonbob.web.ajax;

import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.scm.cvs.CvsServer;
import com.cinnamonbob.web.ActionSupport;
import com.opensymphony.util.TextUtils;

/**
 * An ajax request to test CVS settings and send a fragment of HTML
 * with results.
 */
public class TestCVSAction extends ActionSupport
{
    private String root;
    private String module;
    private String password;

    public void setRoot(String root)
    {
        this.root = root;
    }

    public void setModule(String module)
    {
        this.module = module;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String execute()
    {
        if (!TextUtils.stringSet(root))
        {
            addActionError("root is required");
        }

        if (!TextUtils.stringSet(module))
        {
            addActionError("module is required");
        }

        if (!TextUtils.stringSet(password))
        {
            password = null;
        }

        if (hasErrors())
        {
            // We are just testing, we always succeed in testing, even if the
            // result is a test failure!
            return SUCCESS;
        }

        try
        {
            CvsServer server = new CvsServer(root, module, password);
            server.testConnection();
        }
        catch (SCMException e)
        {
            addActionError(e.getMessage());
        }

        return SUCCESS;
    }
}
