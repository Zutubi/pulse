package com.cinnamonbob.core;

/**
 * <class-comment/>
 */
public class ResourceReference implements ResourceAware, ScopeAware, InitComponent
{
    private String name;
    private String version;

    private ResourceRepository repository;
    private Scope scope;

    public void setScope(Scope scope)
    {
        this.scope = scope;
    }

    public void initBeforeChildren() throws FileLoadException
    {
        Resource resource = null;

        if(repository != null)
        {
            resource = repository.getResource(name);
        }

        if(resource == null)
        {
            throw new FileLoadException("Reference to undefined resource '" + name + "'");
        }

        scope.add(resource.getProperties());

        if(version != null)
        {
            ResourceVersion resourceVersion = resource.getVersion(version);

            if(resourceVersion == null)
            {
                throw new FileLoadException("Reference to undefined version '" + version + "' of resource '" + name + "'");
            }

            scope.add(resourceVersion.getProperties());
        }
    }

    public void initAfterChildren()
    {
        // Nothing to do
    }

    public void setResourceRepository(ResourceRepository repo)
    {
        this.repository = repo;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }
}
