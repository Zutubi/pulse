package com.cinnamonbob.model;

import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.scm.SCMServer;
import com.cinnamonbob.scm.cvs.CvsServer;

/**
 *
 *
 */
public class Cvs extends Scm
{
    private static final String ROOT = "cvs.root";
    private static final String PASS = "cvs.password";
    private static final String MODULE = "cvs.module";

    public SCMServer createServer() throws SCMException
    {
        return new CvsServer(getRoot(), getModule());
    }

    public String getModule()
    {
        return getProperties().getProperty(MODULE);
    }

    public void setModule(String str)
    {
        getProperties().setProperty(MODULE, str);
    }

    public String getRoot()
    {
        return getProperties().getProperty(ROOT);
    }

    public void setRoot(String cvsRoot)
    {
        getProperties().setProperty(ROOT, cvsRoot);
    }

    public String getPassword()
    {
        return getProperties().getProperty(PASS);
    }

    public void setPassword(String password)
    {
        getProperties().setProperty(PASS, password);
    }
}
