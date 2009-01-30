package com.zutubi.pulse.servercore.jetty;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.tove.security.AccessManager;
import static org.mockito.Mockito.*;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;

import java.io.IOException;

public class SecurityHandlerTest extends PulseTestCase
{
    private SecurityHandler handler;

    private HttpRequest request;
    private HttpResponse response;
    private AccessManager accessManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        request = mock(HttpRequest.class);
        response = mock(HttpResponse.class);

        accessManager = mock(AccessManager.class);

        handler = new SecurityHandler();
        handler.setAccessManager(accessManager);
    }

    public void testAuthorised() throws IOException
    {
        stub(accessManager.hasPermission(anyString(), anyObject())).toReturn(true);

        handler.handle("path", "params", request, response);

        verify(request, times(0)).setHandled(anyBoolean());
    }

    public void testForbidden() throws IOException
    {
        stub(accessManager.hasPermission(anyString(), anyObject())).toReturn(false);

        handler.handle("path", "params", request, response);

        verifyForbidden();
    }

    private void verifyForbidden() throws IOException
    {
        verify(request, times(1)).setHandled(true);
        verify(response, times(1)).sendError(HttpResponse.__403_Forbidden);
    }
}
