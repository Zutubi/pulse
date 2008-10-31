package com.zutubi.i18n.context;

/**
 * The context resolver is used to 'resolve' a context into a list
 * of resource names specific to that context.
 */
public interface ContextResolver<T extends Context>
{
    /**
     * Resolve the specified context into a list of resource names.
     *
     * @param context the context of interest.
     *
     * @return an array of strings representing the resolved resource
     * names.
     */
    String[] resolve(T context);

    /**
     * Get the context type supported by this context resolver.  This
     * is used by the bundle manager to identify the resolvers for a
     * specific context. 
     *
     * @return the context class.
     */
    Class<T> getContextType();
}
