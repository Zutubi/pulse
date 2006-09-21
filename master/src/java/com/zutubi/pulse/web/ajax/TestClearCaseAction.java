package com.zutubi.pulse.web.ajax;

import com.zutubi.pulse.model.Scm;
import com.zutubi.pulse.model.P4;
import com.zutubi.pulse.model.ClearCase;

/**
 * An ajax request to test perforce settings and send a fragment of HTML
 * with results.
 */
public class TestClearCaseAction extends BaseTestScmAction
{
    private String url;
    private String viewstore;
    private boolean useVWSParameter;
    private boolean clearCaseLT;

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getViewstore()
    {
        return viewstore;
    }

    public void setViewstore(String viewstore)
    {
        this.viewstore = viewstore;
    }

    public boolean isUseVWSParameter()
    {
        return useVWSParameter;
    }

    public void setUseVWSParameter(boolean useVWSParameter)
    {
        this.useVWSParameter = useVWSParameter;
    }

    public boolean isClearCaseLT()
    {
        return clearCaseLT;
    }

    public void setClearCaseLT(boolean clearCaseLT)
    {
        this.clearCaseLT = clearCaseLT;
    }

    public Scm getScm()
    {
        ClearCase clearCaseConnection = new ClearCase();
        clearCaseConnection.setUrl(url);
        clearCaseConnection.setViewstore(viewstore);
        clearCaseConnection.setUseVWSParameter(useVWSParameter);
        clearCaseConnection.setClearCaseLT(clearCaseLT);
        return clearCaseConnection;
    }
}
