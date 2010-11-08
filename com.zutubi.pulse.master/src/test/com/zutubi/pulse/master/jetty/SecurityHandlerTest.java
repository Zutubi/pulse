package com.zutubi.pulse.master.jetty;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.Role;
import com.zutubi.pulse.master.security.AnonymousActor;
import com.zutubi.pulse.master.security.Principle;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.security.Actor;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.*;

public class SecurityHandlerTest extends PulseTestCase
{
    private SecurityHandler handler;

    private HttpRequest request;
    private HttpResponse response;
    private AccessManager accessManager;
    private AuthenticationEntryPoint basicEndPoint;
    
    protected void setUp() throws Exception
    {
        super.setUp();

        request = mock(HttpRequest.class);
        response = mock(HttpResponse.class);
        basicEndPoint = mock(AuthenticationEntryPoint.class);

        accessManager = mock(AccessManager.class);

        handler = new SecurityHandler();
        handler.setAccessManager(accessManager);
        handler.setBasicEntryPoint(basicEndPoint);
    }

    public void testAuthorised() throws IOException, ServletException
    {
        stub(accessManager.hasPermission(anyString(), anyObject())).toReturn(true);

        handler.handle("path", "params", request, response);

        verify(request, times(0)).setHandled(anyBoolean());
        verify(basicEndPoint, times(0)).commence((HttpServletRequest)anyObject(), (HttpServletResponse)anyObject(), (AuthenticationException)anyObject());
    }

    public void testForbidden() throws IOException, ServletException
    {
        Actor user = new Principle(1, "name", "pass");
        stub(accessManager.getActor()).toReturn(user);
        stub(accessManager.hasPermission(anyString(), anyObject())).toReturn(false);

        handler.handle("path", "params", request, response);

        verify(request, times(1)).setHandled(true);
        verify(response, times(1)).sendError(HttpResponse.__403_Forbidden);
        verify(basicEndPoint, times(0)).commence((HttpServletRequest)anyObject(), (HttpServletResponse)anyObject(), (AuthenticationException)anyObject());
    }

    public void testUnauthorised() throws IOException, ServletException
    {
        List<GrantedAuthority> list = new LinkedList(Arrays.asList(new GrantedAuthorityImpl(Role.ANONYMOUS)));
        Actor anonymous = new AnonymousActor(new AnonymousAuthenticationToken("key", "principal", list));
        stub(accessManager.getActor()).toReturn(anonymous);
        stub(accessManager.hasPermission(anyString(), anyObject())).toReturn(false);

        handler.handle("path", "params", request, response);

        verify(request, times(1)).setHandled(true);
        // verify(response, times(1)).sendError(HttpResponse.__401_Unauthorized); - this will be handled by the end point, so will not appear here.
        verify(basicEndPoint, times(1)).commence((HttpServletRequest)anyObject(), (HttpServletResponse)anyObject(), (AuthenticationException)anyObject());
    }
}
