package com.zutubi.pulse.xmlrpc;

import com.zutubi.pulse.api.APIAuthenticationToken;
import com.zutubi.pulse.util.logging.Logger;
import org.apache.xmlrpc.AuthenticationFailed;
import org.apache.xmlrpc.ParseFailed;
import org.apache.xmlrpc.XmlRpc;
import org.apache.xmlrpc.XmlRpcContext;
import org.apache.xmlrpc.XmlRpcHandlerMapping;
import org.apache.xmlrpc.XmlRpcServerRequest;
import org.apache.xmlrpc.XmlRpcWorker;

import java.io.InputStream;
import java.util.Vector;

/**
 * An xml rpc worker implementation that handles logging and statistical data gathering for the xml rpc
 * interface.
 *
 */
public class PulseXmlRpcWorker extends XmlRpcWorker
{
    private static final Logger LOG = Logger.getLogger(PulseXmlRpcWorker.class);

    public PulseXmlRpcWorker(XmlRpcHandlerMapping handlerMapping)
    {
        super(handlerMapping);
    }

    protected static Object invokeHandler(Object handler, XmlRpcServerRequest request, XmlRpcContext context) throws Exception
    {
        String userName = extractUserName(request);
        
        // statistics:
        //  gather the method, start time and duration of execution.
        //  we can then plot what was run, how often it was run, and how long it took to complete over a period of time.

        String methodName = request.getMethodName();
        long startTime = System.currentTimeMillis();

        LOG.info("\"" + userName + "\" - " + methodName);

        // now we delegate back to the libraries implementation.
        Exception exception = null;
        Object response = null;
        try
        {
            response = XmlRpcWorker.invokeHandler(handler, request, context);
        }
        catch (Exception e)
        {
            exception = e;
        }
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        boolean successful = exception == null;
        
        if (exception != null)
        {
            throw exception;
        }
        return response;
    }

    private static String extractUserName(XmlRpcServerRequest request)
    {
        try
        {
            String userName = "anonymous";
            Vector params = request.getParameters();

            if (isLoginRequest(request))
            {
                // first parameter defines who is attempting to login.
                userName = (String) params.get(0);
            }
            else if (params != null && params.size() > 0)
            {
                // all other authenticated calls will start with a token parameter.  Decode the token to
                // extract the name.
                APIAuthenticationToken token = APIAuthenticationToken.decode((String)params.get(0));
                userName = token.getUsername();
            }
            return userName;
        }
        catch (Exception e)
        {
            return "unknown";
        }
    }

    private static boolean isLoginRequest(XmlRpcServerRequest request)
    {
        String methodName = request.getMethodName();
        Vector params = request.getParameters();
        return methodName.endsWith(".login") && params.size() == 2;
    }

    // fully copied from XmlRcpWorker.  This is required to ensure that our implementation of the invokeHandler is called.
    public byte[] execute(InputStream is, XmlRpcContext context)
    {
        long now = 0;

        if (XmlRpc.debug)
        {
            now = System.currentTimeMillis();
        }

        try
        {
            XmlRpcServerRequest request = requestProcessor.decodeRequest(is);
            Object handler = handlerMapping.getHandler(request.getMethodName());
            Object response = invokeHandler(handler, request, context);
            return responseProcessor.encodeResponse(response, requestProcessor.getEncoding());
        }
        catch (AuthenticationFailed alertCallerAuth)
        {
            throw alertCallerAuth;
        }
        catch (ParseFailed alertCallerParse)
        {
            throw alertCallerParse;
        }
        catch (Exception x)
        {
            if (XmlRpc.debug)
            {
                x.printStackTrace();
            }
            return responseProcessor.encodeException(x, requestProcessor.getEncoding());
        }
        finally
        {
            if (XmlRpc.debug)
            {
                System.out.println("Spent " + (System.currentTimeMillis() - now) + " millis in request/process/response");
            }
        }
    }
}
