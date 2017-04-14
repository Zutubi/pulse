/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.servercore.xmlrpc;

import com.zutubi.pulse.servercore.api.APIAuthenticationToken;
import com.zutubi.util.logging.Logger;
import org.apache.xmlrpc.*;

import java.io.InputStream;
import java.util.Vector;

public class PulseXmlRpcWorker extends XmlRpcWorker
{
    private static final Logger LOG = Logger.getLogger(PulseXmlRpcWorker.class);

    public PulseXmlRpcWorker(XmlRpcHandlerMapping handlerMapping)
    {
        super(handlerMapping);
        responseProcessor = new PulseXmlRpcResponseProcessor();
    }

    protected static Object invokeHandler(Object handler, XmlRpcServerRequest request, XmlRpcContext context) throws Exception
    {
        String userName = extractUserName(request);

        // statistics:
        //  gather the method, start time and duration of execution.
        //  we can then plot what was run, how often it was run, and how long it took to complete over a period of time.

        String methodName = request.getMethodName();

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
                APIAuthenticationToken token = new APIAuthenticationToken((String)params.get(0));
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
