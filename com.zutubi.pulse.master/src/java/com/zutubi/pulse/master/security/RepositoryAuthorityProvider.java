package com.zutubi.pulse.master.security;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.dependency.RepositoryAttributes;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.model.Role;
import com.zutubi.pulse.master.tove.config.admin.RepositoryConfiguration;
import com.zutubi.pulse.master.tove.config.group.GroupConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.servercore.events.system.SystemStartedListener;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.security.AuthorityProvider;
import com.zutubi.tove.security.DefaultAccessManager;
import org.mortbay.http.HttpRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.zutubi.pulse.master.bootstrap.WebManager.REPOSITORY_PATH;
import static com.zutubi.tove.security.AccessManager.ACTION_VIEW;
import static com.zutubi.tove.security.AccessManager.ACTION_WRITE;
import static java.util.Arrays.asList;

/**
 * The repository authority provider determines the authorities that
 * are allowed to access a particular path within the pulse artifact
 * repository.
 * <p/>
 * There are two stages to the determination of the allowed authorities.
 * a) if the requested path can be mapped to a project, then the projects
 * authorities are used.
 * b) otherwise, the default authorities configured via the settings/repository
 * are applied.
 */
public class RepositoryAuthorityProvider implements AuthorityProvider<HttpInvocation>
{
    private static Set<String> NOT_ALLOWED = new HashSet<String>();

    private static final List<String> WRITE_METHODS = asList(
            HttpRequest.__PUT
    );

    private static final List<String> READ_METHODS = asList(
            HttpRequest.__GET, HttpRequest.__HEAD
    );

    private RepositoryAttributes repositoryAttributes;
    private ConfigurationProvider configurationProvider;
    private ProjectConfigurationAuthorityProvider projectAuthorityProvider;

    public Set<String> getAllowedAuthorities(String method, HttpInvocation resource)
    {
        if (!isMethodSupported(resource.getMethod()))
        {
            return NOT_ALLOWED;
        }

        String action = isWriteMethod(resource.getMethod()) ? ACTION_WRITE : ACTION_VIEW;

        HashSet<String> allowedAuthorities = new HashSet<String>();

        ProjectConfiguration project = lookupProject(getRelativePath(resource));

        // A) If we have a project, delegate to the project authority provider.
        if (project != null)
        {
            if (projectAuthorityProvider != null)
            {
                allowedAuthorities.addAll(projectAuthorityProvider.getAllowedAuthorities(action, project));
            }
            // If we can not delegate, then we take the safe approach and disallow access.
        }
        else
        {
            // B) Use the configured defaults to determine the allowed authorities.
            if (configurationProvider != null)
            {
                RepositoryConfiguration repositoryConfiguration = configurationProvider.get(RepositoryConfiguration.class);
                if (action.equals(ACTION_WRITE))
                {
                    allowedAuthorities.addAll(getAuthorities(repositoryConfiguration.getWriteAccess()));
                }
                else
                {
                    allowedAuthorities.addAll(getAuthorities(repositoryConfiguration.getReadAccess()));
                }
            }
        }

        // guest and anonymous are equivalent enough in this case to want to use both interchangeably.
        if (allowedAuthorities.contains(Role.GUEST))
        {
            allowedAuthorities.add(Role.ANONYMOUS);
        }

        return allowedAuthorities;
    }

    /**
     * Attempt to map the path to a project.  The implied path format is
     * /(organisation/)projectName/...., where the org component is optional.
     *
     * @param path the path that may identify a project.
     * @return a project configuraiton if the path can be mapped to a project,
     *         null otherwise.
     */
    private ProjectConfiguration lookupProject(String path)
    {
        ProjectConfiguration project = null;

        Map<String, String> attributes = repositoryAttributes.getMergedAttributes(path);
        if (attributes.containsKey(RepositoryAttributes.PROJECT_HANDLE))
        {
            long handle = Long.valueOf(attributes.get(RepositoryAttributes.PROJECT_HANDLE));
            project = configurationProvider.get(handle, ProjectConfiguration.class);
        }
        return project;
    }

    /**
     * Get the requested path, relative to the root of the repository.
     *
     * @param resource the requested resource
     * @return the path
     */
    private String getRelativePath(HttpInvocation resource)
    {
        String path = resource.getPath();
        if (path.startsWith(REPOSITORY_PATH))
        {
            path = path.substring(REPOSITORY_PATH.length());
        }
        return path;
    }

    private Set<String> getAuthorities(List<GroupConfiguration> groups)
    {
        Set<String> authorities = new HashSet<String>();
        if (groups != null)
        {
            for (GroupConfiguration group : groups)
            {
                authorities.add(group.getDefaultAuthority());
                authorities.addAll(asList(group.getGrantedAuthorities()));
            }
        }
        return authorities;
    }

    /**
     * Check if the specified http method is one that this authority provider is able
     * to deal with.
     *
     * @param method the http method in question.
     * @return true if the method is dealt with by this provider, false otherwise.
     */
    private boolean isMethodSupported(String method)
    {
        return READ_METHODS.contains(method) || WRITE_METHODS.contains(method);
    }

    /**
     * Check if the specified http method represents a write method.
     *
     * @param method the http method in question
     * @return true if the method is a write method, false otherwise.
     */
    private boolean isWriteMethod(String method)
    {
        return WRITE_METHODS.contains(method);
    }

    public void setAccessManager(DefaultAccessManager accessManager)
    {
        accessManager.registerAuthorityProvider(HttpInvocation.class, this);
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setProjectConfigurationAuthorityProvider(ProjectConfigurationAuthorityProvider projectAuthorityProvider)
    {
        this.projectAuthorityProvider = projectAuthorityProvider;
    }

    public void setRepositoryAttributes(RepositoryAttributes repositoryAttributes)
    {
        this.repositoryAttributes = repositoryAttributes;
    }

    public void setEventManager(EventManager eventManager)
    {
        // The two providers are not available when the security system is initialised and so
        // need to be manually wired on system startup.
        eventManager.register(new SystemStartedListener()
        {
            public void systemStarted()
            {
                setConfigurationProvider((ConfigurationProvider) SpringComponentContext.getBean("configurationProvider"));
                setProjectConfigurationAuthorityProvider((ProjectConfigurationAuthorityProvider) SpringComponentContext.getBean("projectConfigurationAuthorityProvider"));
                setRepositoryAttributes((RepositoryAttributes) SpringComponentContext.getBean("repositoryAttributes"));
            }
        });
    }
}