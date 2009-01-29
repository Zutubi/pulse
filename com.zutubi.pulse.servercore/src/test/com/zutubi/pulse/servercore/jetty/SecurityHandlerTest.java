package com.zutubi.pulse.servercore.jetty;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import org.acegisecurity.Authentication;
import static org.mockito.Mockito.*;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.HttpRequest;

import java.io.IOException;

public class SecurityHandlerTest extends PulseTestCase
{
    private SecurityHandler handler;

    private HttpRequest request;
    private HttpResponse response;
    private PrivilegeEvaluator evaluator;

    protected void setUp() throws Exception
    {
        super.setUp();

        request = mock(HttpRequest.class);
        response = mock(HttpResponse.class);

        evaluator = mock(PrivilegeEvaluator.class);

        handler = new SecurityHandler();
        handler.setPrivilegeEvaluator(evaluator);
    }

    public void testNullAuthentication() throws IOException
    {
        stub(evaluator.isAllowed((HttpInvocation)anyObject(), (Authentication) isNull())).toReturn(false);

        handler.handle("path", "params", request, response);

        verifyForbidden();
    }

    public void testAuthorised() throws IOException
    {
        stub(evaluator.isAllowed((HttpInvocation)anyObject(), (Authentication) anyObject())).toReturn(true);

        handler.handle("path", "params", request, response);

        verify(request, times(0)).setHandled(anyBoolean());
    }

    public void testForbidden() throws IOException
    {
        stub(evaluator.isAllowed((HttpInvocation)anyObject(), (Authentication) anyObject())).toReturn(false);

        handler.handle("path", "params", request, response);

        verifyForbidden();
    }

    private void verifyForbidden() throws IOException
    {
        verify(request, times(1)).setHandled(true);
        verify(response, times(1)).sendError(HttpResponse.__403_Forbidden);
    }
}
