package com.zutubi.pulse.web.ajax;

import com.opensymphony.util.TextUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.pulse.web.ActionSupport;
import org.apache.xmlrpc.XmlRpcClient;

import java.util.Vector;

/**
 */
public class RequestEvaluationAction extends ActionSupport
{
    private boolean success = false;
    private String name;
    private String email;
    private String license;
    private String error;

    public void setName(String name)
    {
        this.name = name;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public String getLicense()
    {
        return license;
    }

    public String getError()
    {
        return error;
    }

    public String execute()
    {
        if(!TextUtils.stringSet(name))
        {
            error = getText("license.full.name.required");
            return SUCCESS;
        }

        if(!TextUtils.verifyEmail(email))
        {
            error = getText("license.email.invalid");
            return SUCCESS;
        }

        try
        {
            XmlRpcClient client = new XmlRpcClient("http://zutubi.com/xmlrpc/");
            Vector<String> args = new Vector<String>(2);
            args.add(name);
            args.add(email);

            license = (String) client.execute("evaluation", args);
            license = StringUtils.wrapString(license, 50, null);
            success = true;
        }
        catch (Exception e)
        {
            error = e.toString();
        }

        return SUCCESS;
    }
}
