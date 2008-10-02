package com.zutubi.pulse.web.ajax;

import com.zutubi.pulse.master.tove.config.setup.RequestLicenseConfiguration;
import com.zutubi.tove.webwork.TransientAction;
import com.zutubi.util.StringUtils;
import org.apache.xmlrpc.XmlRpcClient;

import java.util.Vector;

/**
 */
public class RequestLicenseAction extends TransientAction<RequestLicenseConfiguration>
{
    private SimpleResult result;

    public RequestLicenseAction()
    {
        super("init/requestLicense", true);
    }

    public SimpleResult getResult()
    {
        return result;
    }

    protected RequestLicenseConfiguration initialise()
    {
        return new RequestLicenseConfiguration();
    }

    protected String complete(RequestLicenseConfiguration instance)
    {
        try
        {
            XmlRpcClient client = new XmlRpcClient("http://zutubi.com/xmlrpc/");
            Vector<String> args = new Vector<String>(2);
            args.add(instance.getName());
            args.add(instance.getEmail());

            String license = (String) client.execute("evaluation", args);
            license = StringUtils.wrapString(license, 50, null);
            result = new SimpleResult(true, license);
        }
        catch (Exception e)
        {
            result = new SimpleResult(false, e.toString());
        }
        return SUCCESS;
    }

}
