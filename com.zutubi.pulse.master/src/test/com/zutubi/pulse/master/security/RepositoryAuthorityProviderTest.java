package com.zutubi.pulse.master.security;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.PROJECTS_SCOPE;
import com.zutubi.pulse.master.tove.config.admin.RepositoryConfiguration;
import com.zutubi.pulse.master.tove.config.group.UserGroupConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import static com.zutubi.tove.security.AccessManager.ACTION_VIEW;
import static com.zutubi.tove.security.AccessManager.ACTION_WRITE;
import static com.zutubi.util.CollectionUtils.asSet;
import static org.mockito.Mockito.*;
import static org.mortbay.http.HttpRequest.__GET;
import static org.mortbay.http.HttpRequest.__PUT;

import java.util.Set;

public class RepositoryAuthorityProviderTest extends PulseTestCase
{
    private RepositoryAuthorityProvider provider;
    private ConfigurationProvider configurationProvider;
    private ProjectConfigurationAuthorityProvider delegateProvider;

    private int handle = 1;
    private RepositoryConfiguration repositoryConfiguration;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        delegateProvider = mock(ProjectConfigurationAuthorityProvider.class);

        configurationProvider = mock(ConfigurationProvider.class);
        repositoryConfiguration = new RepositoryConfiguration();
        stub(configurationProvider.get(RepositoryConfiguration.class)).toReturn(repositoryConfiguration);

        provider = new RepositoryAuthorityProvider();
        provider.setConfigurationProvider(configurationProvider);
        provider.setProjectConfigurationAuthorityProvider(delegateProvider);
    }

    public void testPathWithoutOrg()
    {
        ProjectConfiguration project = newProject(null, "project");
        provider.getAllowedAuthorities(null, newInvocation(__PUT, "project/ixy.xml"));
        verify(delegateProvider, times(1)).getAllowedAuthorities(ACTION_WRITE, project);
    }

    public void testPathWithMismatchingOrg()
    {
        newProject("orgA", "project");
        provider.getAllowedAuthorities(null, newInvocation(__PUT, "orgB/project/ixy.xml"));
        verify(delegateProvider, times(0)).getAllowedAuthorities(eq(ACTION_WRITE), (ProjectConfiguration) anyObject());
    }

    public void testPathWithNoMatches()
    {
        provider.getAllowedAuthorities(null, newInvocation(__PUT, "org/project/ixy.xml"));
        verify(delegateProvider, times(0)).getAllowedAuthorities(eq(ACTION_WRITE), (ProjectConfiguration) anyObject());
    }

    public void testDelegation_WriteRequest()
    {
        ProjectConfiguration project = newProject("org", "project");
        stub(delegateProvider.getAllowedAuthorities(ACTION_WRITE, project)).toReturn(asSet("write"));

        Set<String> allowedAuthorities = provider.getAllowedAuthorities(null, newInvocation(__PUT, "org/project/ixy.xml"));
        assertEquals(1, allowedAuthorities.size());
        assertTrue(allowedAuthorities.contains("write"));

        verify(delegateProvider, times(1)).getAllowedAuthorities(ACTION_WRITE, project);
    }

    public void testDelegation_ReadRequest()
    {
        ProjectConfiguration project = newProject("org", "project");
        stub(delegateProvider.getAllowedAuthorities(ACTION_VIEW, project)).toReturn(asSet("read"));

        Set<String> allowedAuthorities = provider.getAllowedAuthorities(null, newInvocation(__GET, "org/project/ixy.xml"));
        assertEquals(1, allowedAuthorities.size());
        assertTrue(allowedAuthorities.contains("read"));

        verify(delegateProvider, times(1)).getAllowedAuthorities(ACTION_VIEW, project);
    }

    public void testUnknown_WriteRequest()
    {
        addDefaultWriteGroup("writers");

        Set<String> allowedAuthorities = provider.getAllowedAuthorities(null, newInvocation(__PUT, "org/project/ixy.xml"));
        assertEquals(1, allowedAuthorities.size());
        assertTrue(allowedAuthorities.contains("group:writers"));
    }

    public void testUnknown_ReadRequest()
    {
        addDefaultReadGroup("readers");

        Set<String> allowedAuthorities = provider.getAllowedAuthorities(null, newInvocation(__GET, "org/project/ixy.xml"));
        assertEquals(1, allowedAuthorities.size());
        assertTrue(allowedAuthorities.contains("group:readers"));
    }

    public void testUnknownMethod()
    {
        newProject("org", "project");
        Set<String> allowedAuthorities = provider.getAllowedAuthorities(null, newInvocation("unknown", "org/project/ivy.xml"));
        assertEquals(0, allowedAuthorities.size());
        
        verify(configurationProvider, times(0)).get(RepositoryConfiguration.class);
        verify(delegateProvider, times(0)).getAllowedAuthorities(anyString(), (ProjectConfiguration) anyObject());
    }

    private void addDefaultWriteGroup(String name)
    {
        UserGroupConfiguration group = new UserGroupConfiguration(name);
        repositoryConfiguration.getWriteAccess().add(group);
    }

    private void addDefaultReadGroup(String name)
    {
        UserGroupConfiguration group = new UserGroupConfiguration(name);
        repositoryConfiguration.getReadAccess().add(group);
    }

    private ProjectConfiguration newProject(String org, String name)
    {
        ProjectConfiguration project = new ProjectConfiguration(org, name);
        project.setHandle(handle++);
        stub(configurationProvider.get(PROJECTS_SCOPE + "/" + name, ProjectConfiguration.class)).toReturn(project);
        return project;
    }

    private HttpInvocation newInvocation(String method, String path)
    {
        HttpInvocation invocation = mock(HttpInvocation.class);
        stub(invocation.getPath()).toReturn(path);
        stub(invocation.getMethod()).toReturn(method);
        return invocation;
    }
    
}
