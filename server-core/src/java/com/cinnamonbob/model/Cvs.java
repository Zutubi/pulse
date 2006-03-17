package com.cinnamonbob.model;

import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.scm.SCMServer;
import com.cinnamonbob.scm.cvs.CvsServer;

/**
 * The CVS object defines the configuration properties required to communicate with a
 * cvs repository.
 *
 */
public class Cvs extends Scm
{
    /**
     * The cvs root property string.
     */
    private static final String ROOT = "cvs.root";

    /**
     * The cvs connection password property string.
     */
    private static final String PASS = "cvs.password";

    /**
     * The cvs module property string.
     */
    private static final String MODULE = "cvs.module";

    private static final String QUIET_PERIOD = "cvs.quiet";

    public SCMServer createServer() throws SCMException
    {
        return new CvsServer(getRoot(), getModule(), getPassword());
    }

    /**
     * The module defines the module within the cvs repository.
     *
     */
    public String getModule()
    {
        return getProperties().getProperty(MODULE);
    }

    public void setModule(String str)
    {
        getProperties().setProperty(MODULE, str);
    }

    /**
     * The root property is the cvs root required to identify the cvs repository that will
     * be connected to.
     *
     */
    public String getRoot()
    {
        return getProperties().getProperty(ROOT);
    }

    public void setRoot(String cvsRoot)
    {
        getProperties().setProperty(ROOT, cvsRoot);
    }

    /**
     * This password is used when the connection to the cvs server requires authentication,
     * such as is the case when SSH is being used.
     *
     * @return the password.
     */
    public String getPassword()
    {
        return getProperties().getProperty(PASS);
    }

    public void setPassword(String password)
    {
        getProperties().setProperty(PASS, password);
    }

    public long getQuietPeriod()
    {
        if (getProperties().containsKey(QUIET_PERIOD))
        {
            return Long.parseLong(getProperties().getProperty(QUIET_PERIOD));
        }
        return 0;
    }

    public void setQuietPeriod(long milliseconds)
    {
        getProperties().setProperty(QUIET_PERIOD, Long.toString(milliseconds));
    }
}
