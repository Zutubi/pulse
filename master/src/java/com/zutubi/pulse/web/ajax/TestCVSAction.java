/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.ajax;

import com.zutubi.pulse.model.Cvs;
import com.zutubi.pulse.model.Scm;

/**
 * An ajax request to test CVS settings and send a fragment of HTML
 * with results.
 */
public class TestCVSAction extends BaseTestScmAction
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

    public Scm getScm()
    {
        Cvs cvsConnection = new Cvs();
        cvsConnection.setRoot(root);
        cvsConnection.setModule(module);
        cvsConnection.setPassword(password);
        return cvsConnection;
    }
}
