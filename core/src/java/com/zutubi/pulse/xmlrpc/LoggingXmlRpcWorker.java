package com.zutubi.pulse.xmlrpc;

import org.apache.xmlrpc.XmlRpcWorker;
import org.apache.xmlrpc.XmlRpcHandlerMapping;
import org.apache.xmlrpc.XmlRpcServerRequest;
import org.apache.xmlrpc.XmlRpcContext;
import org.apache.xmlrpc.XmlRpc;
import org.apache.xmlrpc.AuthenticationFailed;
import org.apache.xmlrpc.ParseFailed;
import org.apache.commons.codec.binary.Base64;

import java.util.Vector;
import java.io.InputStream;

import com.zutubi.util.logging.Logger;

public class LoggingXmlRpcWorker extends XmlRpcWorker
{
    private static final Logger LOG = Logger.getLogger(LoggingXmlRpcWorker.class);

    public LoggingXmlRpcWorker(XmlRpcHandlerMapping handlerMapping)
    {
        super(handlerMapping);
    }

    protected static Object invokeHandler(Object handler, XmlRpcServerRequest request, XmlRpcContext context) throws Exception
    {
        // do some logging here :).
        String userName = "anonymous";
        Vector params = request.getParameters();
        String methodName = request.getMethodName().substring(request.getMethodName().indexOf(".") + 1);
        if (methodName.equals("login"))
        {
            userName = (String) params.get(0);
        }
        else if (params != null && params.size() > 0)
        {
            String token = (String)params.get(0);
            String decodedToken = new String(Base64.decodeBase64(token.getBytes()));
            if (decodedToken.indexOf(":") != -1)
            {
                userName = decodedToken.substring(0, decodedToken.indexOf(":"));
            }
        }
        LOG.info("\"" + userName + "\" - " + request.getMethodName());

        return XmlRpcWorker.invokeHandler(handler, request, context);
    }

    // fully copied from XmlRcpWorker.
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
            Object handler = handlerMapping.getHandler(request.
                                                       getMethodName());
            Object response = invokeHandler(handler, request, context);
            return responseProcessor.encodeResponse
                (response, requestProcessor.getEncoding());
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
            return responseProcessor.encodeException
                (x, requestProcessor.getEncoding());
        }
        finally
        {
            if (XmlRpc.debug)
            {
                System.out.println("Spent " + (System.currentTimeMillis() - now)
                                   + " millis in request/process/response");
            }
        }
    }
}
