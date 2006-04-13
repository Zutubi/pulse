/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.util.Constants;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.SCMServer;
import com.zutubi.pulse.scm.cvs.CvsServer;
import com.opensymphony.util.TextUtils;

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

    /**
     * The cvs quiet period property string.
     */
    private static final String QUIET_PERIOD = "cvs.quiet";

    private static final String BRANCH = "cvs.branch";

    public SCMServer createServer() throws SCMException
    {
        // use a manual autowire here since this object itself is not wired, and so
        // does not have access to the object factory.
        CvsServer server = new CvsServer(this);
        ComponentContext.autowire(server);
        return server;
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

    public String getBranch()
    {
        return getProperties().getProperty(BRANCH);
    }

    public void setBranch(String branch)
    {
        getProperties().setProperty(BRANCH, branch);
    }

    /**
     * The quiet period is an amount of time (in milliseconds) after a cvs checkin that
     * the scm monitoring process will wait before generating an scm change event. During this
     * period is is required that no other checkins / changes are made in cvs.
     *
     * @return time in milliseconds.
     */
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

    public void setQuietPeriod(String minutes, String seconds)
    {
        // convert the mins / secs to long and set.
        long quietPeriod = 0;
        if (TextUtils.stringSet(minutes))
        {
            quietPeriod += Integer.parseInt(minutes) * Constants.MINUTE;
        }
        if (TextUtils.stringSet(seconds))
        {
            quietPeriod += Integer.parseInt(seconds) * Constants.SECOND;
        }
        setQuietPeriod(quietPeriod);
    }

    public String getQuietPeriodMinutes()
    {
        long quietPeriod = getQuietPeriod();
        long mins = (quietPeriod / Constants.MINUTE);
        if (mins > 0)
        {
            return Long.toString(mins);
        }
        return null;
    }

    public String getQuietPeriodSeconds()
    {
        long quietPeriod = getQuietPeriod();
        long secs = (quietPeriod % Constants.MINUTE) / Constants.SECOND;
        if (secs > 0)
        {
            return Long.toString(secs);
        }
        return null;
    }
}
