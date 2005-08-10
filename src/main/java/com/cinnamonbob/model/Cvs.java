package com.cinnamonbob.model;

import com.cinnamonbob.scm.SCMServer;
import com.cinnamonbob.scm.SCMException;

/**
 *
 *
 */
public class Cvs extends Scm
{
    private static final String ROOT = "cvs.root";
    private static final String PASS = "cvs.password";

    public SCMServer createServer() throws SCMException
    {
        return null;
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
