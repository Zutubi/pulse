package com.cinnamonbob.spring.web.context;

/**
 * Web context loader that creates a bridge to bobs built in application
 * context.
 *
 */
public class ContextLoaderListener extends org.springframework.web.context.ContextLoaderListener
{
    /**
     * Create the ContextLoader to use. Can be overridden in subclasses.
     * @return the new ContextLoader
     */
    protected ContextLoader createContextLoader() {
        return new ContextLoader();
    }

}
