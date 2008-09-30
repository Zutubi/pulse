package com.zutubi.tove.rpc;

import org.apache.xmlrpc.XmlRpcHandler;

import java.util.Vector;

/**
 *
 *
 */
public class DynamicXmlRpcHandler implements XmlRpcHandler
{
    public Object execute(String method, Vector params) throws Exception
    {
        // examine the name of the method to determine what the request is asking.

        if (method.equals("help"))
        {
            return handleHelp(method, params);
        }

        // is getter or setter?
        if (method.startsWith("get"))
        {
            return handleGetter(method, params);
        }
        else if (method.startsWith("set"))
        {
            return handleSetter(method, params);
        }

        // are there any other types we want to handle? delegate to some business method processes? -> best to
        // leave that to a separate handler.

        // unknown method.
        throw new RuntimeException("Unsupported method " + method);
    }

    private Object handleSetter(String method, Vector params)
    {
        String symbolicName = method.substring(3);

        // extract the scope details from the params so that the entity defined by the symbolic name
        // can be resolved.

        // Class definition = getDefinition(symbolicName);
        // apply data to an instance of the defined type.
        // validationManager.validate(data);
        // if validation is successful, dataSource.setData(x, y, z);
        // else return error response.

        return null;
    }

    private Object handleGetter(String method, Vector params)
    {
        String symbolicName = method.substring(3);

        // extract the scope details from the params so that the entity defined by the symbolic name
        // can be resolved.

        // return dataSource.getData(x, y, z);

        return null;
    }

    protected Object handleHelp(String method, Vector params)
    {
        // generate a list of supported methods and associated documentation where available.
        Vector<String> availableMethods = new Vector<String>();

        availableMethods.add("help");

        return availableMethods;
    }
}
