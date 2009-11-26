package com.zutubi.pulse.master.tove.velocity;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

import java.io.InputStream;

/**
 * A custom resource loader that will look for a resource within the context
 * of a specific class.  That class is specified via the CONTEXT thread local.
 * This is a somewhat round about way to control the velocity resource lookups
 * on each lookup.
 */
public class VelocityClasspathResourceLoader extends ResourceLoader
{
    public final static ThreadLocal<Class> CONTEXT = new ThreadLocal<Class>();

    public void init(ExtendedProperties configuration)
    {
        // noop.
    }

    public InputStream getResourceStream(String source) throws ResourceNotFoundException
    {
        Class context = CONTEXT.get();
        if (context != null)
        {
            return context.getResourceAsStream("/" + source);
        }
        return null;
    }

    public boolean isSourceModified(Resource resource)
    {
        return false;
    }

    public long getLastModified(Resource resource)
    {
        return 0;
    }
}
