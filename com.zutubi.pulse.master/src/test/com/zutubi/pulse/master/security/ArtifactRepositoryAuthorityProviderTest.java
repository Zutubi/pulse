package com.zutubi.pulse.master.security;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import static org.mockito.Mockito.*;

import java.util.Set;

public class ArtifactRepositoryAuthorityProviderTest extends PulseTestCase
{
    private ArtifactRepositoryAuthorityProvider provider;
    private AuthorityDefinitions definitions;
    private HttpInvocation invocation;

    protected void setUp() throws Exception
    {
        super.setUp();

        provider = new ArtifactRepositoryAuthorityProvider();
        definitions = new AuthorityDefinitions();
        provider.setDefinitions(definitions);
        invocation = mock(HttpInvocation.class);
    }

    public void testRootPath()
    {
        addPrivilege("/", "ROLE_USER", "GET");
        stub(invocation.getPath()).toReturn("/some/path.html");

        assertAuthorities("GET", "ROLE_USER");
        assertAuthorities("PUT");
    }

    public void testNestedPath()
    {
        addPrivilege("/some", "ROLE_USER", "GET");
        stub(invocation.getPath()).toReturn("/some/path.html");

        assertAuthorities("GET", "ROLE_USER");
        assertAuthorities("PUT");
    }

    public void testMultipleRoles()
    {
        addPrivilege("/", "ROLE_USER", "GET");
        addPrivilege("/", "ROLE_ANONYMOUS", "GET");
        stub(invocation.getPath()).toReturn("/some/path.html");

        assertAuthorities("GET", "ROLE_USER", "ROLE_ANONYMOUS");
        assertAuthorities("PUT");
    }

    private void addPrivilege(String path, String role, String method)
    {
        definitions.addPrivilege(path, role, method);
    }

    private void assertAuthorities(String method, String... expectedAuthorities)
    {
        Set<String> authorities = provider.getAllowedAuthorities(method, invocation);
        assertEquals(expectedAuthorities.length, authorities.size());
        for (String expectedAuthority : expectedAuthorities)
        {
            assertTrue(authorities.contains(expectedAuthority));
        }
    }
}

