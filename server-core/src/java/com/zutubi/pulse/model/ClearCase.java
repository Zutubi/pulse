package com.zutubi.pulse.model;

import com.zutubi.pulse.scm.SCMServer;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.clearcase.ClearCaseServer;

/**
 * <class-comment/>
 */
public class ClearCase extends Scm
{
    /**
     * Defines the clearcase connection url.
     */
    private static final String URL = "cc.url";

    /**
     * Specify the location of the view store if the default location is not being used.
     */
    private static final String VIEWSTORE = "cc.viewstore";

    /**
     * Specify whether or not the mkview command should use the -vws parameter.
     */
    private static final String VWS = "cc.vws";

    /**
     * Indicate that clearcase lt is being used.
     */
    private static final String CCLT = "cc.lt";

    public ClearCase()
    {
        setUseVWSParameter(true);
        setClearCaseLT(false);
    }

    public SCMServer createServer() throws SCMException
    {
        return new ClearCaseServer(getUrl());
    }

    public String getUrl()
    {
        return (String) getProperties().get(URL);
    }

    public void setUrl(String url)
    {
        getProperties().put(URL, url);
    }

    public String getViewstore()
    {
        return (String) getProperties().get(VIEWSTORE);
    }

    public void setViewstore(String viewStore)
    {
        getProperties().put(VIEWSTORE, viewStore);
    }

    public boolean getUseVWSParameter()
    {
        return Boolean.valueOf((String)getProperties().get(VWS));
    }

    public void setUseVWSParameter(boolean b)
    {
        getProperties().put(VWS, Boolean.valueOf(b).toString());
    }

    public boolean getClearCaseLT()
    {
        return Boolean.valueOf((String)getProperties().get(CCLT));
    }

    public void setClearCaseLT(boolean b)
    {
        getProperties().put(CCLT, Boolean.valueOf(b).toString());
    }
}
