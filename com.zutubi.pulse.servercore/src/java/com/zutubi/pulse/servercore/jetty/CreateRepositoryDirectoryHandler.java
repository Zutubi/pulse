package com.zutubi.pulse.servercore.jetty;

import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.mortbay.util.Resource;

import java.io.File;
import java.io.IOException;

/**
 * When uploading resource to the {@link org.mortbay.http.handler.ResourceHandler}, if the target
 * directory does not exist, the upload will fail.  This handler is designed
 * to specifically get around this limitation by running ahead of the {@link org.mortbay.http.handler.ResourceHandler}
 * and creating the target directory if necessary.
 */
public class CreateRepositoryDirectoryHandler extends AbstractHttpHandler
{
    public void handle(String pathInContext, String pathParams, HttpRequest request, HttpResponse response) throws HttpException, IOException
    {
        String method = request.getMethod();
        if (method.equals(HttpRequest.__PUT))
        {
            Resource resource = getResource(pathInContext);
            if (resource == null)
            {
                return;
            }

            handlePut(request, response, pathInContext, resource);
        }
    }

    private void handlePut(HttpRequest request, HttpResponse response, String pathInContext, Resource resource) throws IOException
    {
        // ignore the case where the resource refers to a directory already.  This case is handled
        // by the ResourceHandler.
        if (!pathInContext.endsWith("/"))
        {
            File dir = resource.getFile().getParentFile();
            if (!dir.isDirectory() && !dir.mkdirs())
            {
                response.sendError(HttpResponse.__500_Internal_Server_Error, "Directories could not be created");
                request.setHandled(true);
                response.commit();
            }
        }
    }

    private Resource getResource(String pathInContext) throws IOException
    {
        return getHttpContext().getResource(pathInContext);
    }
}
