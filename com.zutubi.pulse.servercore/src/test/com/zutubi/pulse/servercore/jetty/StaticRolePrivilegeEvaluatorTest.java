package com.zutubi.pulse.servercore.jetty;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.doReturn;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;

import java.util.List;
import java.util.LinkedList;

public class StaticRolePrivilegeEvaluatorTest extends PulseTestCase
{
    private StaticRolePrivilegeEvaluator evaluator;
    private Authentication auth;
    private HttpInvocation http;

    protected void setUp() throws Exception
    {
        super.setUp();

        evaluator = new StaticRolePrivilegeEvaluator();

        http = mock(HttpInvocation.class);
        auth = mock(Authentication.class);
    }

    public void testNullArguments()
    {
        assertFalse(evaluator.isAllowed(null, null));
        assertFalse(evaluator.isAllowed(http, null));
        assertFalse(evaluator.isAllowed(null, auth));
    }

    public void testNoPrivilegesWithNoInformation()
    {
        assertFalse(evaluator.isAllowed(http, auth));
    }

    public void testPrivileges()
    {
        evaluator.addPrivilege("/", "ROLE_USER", "GET");

        assertPrivileged("/path/to/resource.html", "ROLE_USER", "GET");
        assertNotPrivileged("/path/to/resource.html", "ROLE_USER", "PUT");
        assertNotPrivileged("/path/to/resource.html", "ROLE_ANONYMOUS", "GET");
    }

    private void assertPrivileged(String path, String role, String method)
    {
        assertTrue(isPrivileged(path, role, method));
    }

    private void assertNotPrivileged(String path, String role, String method)
    {
        assertFalse(isPrivileged(path, role, method));
    }

    private boolean isPrivileged(String path, String role, String method)
    {
        stub(http.getMethod()).toReturn(method);
        stub(http.getPath()).toReturn(path);
        stub(auth.getAuthorities()).toReturn(roles(role));

        return evaluator.isAllowed(http, auth);
    }

    private GrantedAuthority[] roles(String... roles)
    {
        List<GrantedAuthority> result = new LinkedList<GrantedAuthority>();
        for (String auth : roles)
        {
            result.add(new GrantedAuthorityImpl(auth));
        }
        return result.toArray(new GrantedAuthority[result.size()]);
    }
}
