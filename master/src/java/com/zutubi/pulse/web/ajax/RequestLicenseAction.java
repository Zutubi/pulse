package com.zutubi.pulse.web.ajax;

import com.zutubi.prototype.webwork.TransientAction;
import com.zutubi.pulse.prototype.config.setup.RequestLicenseConfiguration;
import com.zutubi.util.StringUtils;
import org.apache.xmlrpc.XmlRpcClient;

import java.util.Vector;

/**
 */
public class RequestLicenseAction extends TransientAction<RequestLicenseConfiguration>
{
    private LicenseResult result;

    public RequestLicenseAction()
    {
        super("setup/requestLicense");
    }

    public LicenseResult getResult()
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
            result = new LicenseResult(true, license);
        }
        catch (Exception e)
        {
            result = new LicenseResult(false, e.toString());
        }
        return SUCCESS;
    }

    public static class LicenseResult
    {
        private boolean success;
        private String detail;

        public LicenseResult(boolean success, String detail)
        {
            this.success = success;
            this.detail = detail;
        }

        public boolean isSuccess()
        {
            return success;
        }

        public String getDetail()
        {
            return detail;
        }
    }
}
