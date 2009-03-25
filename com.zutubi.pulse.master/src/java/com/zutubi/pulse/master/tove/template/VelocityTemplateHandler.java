package com.zutubi.pulse.master.tove.template;

import org.apache.velocity.app.VelocityEngine;

import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;

import com.zutubi.i18n.context.*;
import com.zutubi.util.NullaryFunction;
import com.zutubi.util.logging.Logger;

/**
 * A velocity based implementation of the TemplateHandler.  All template lookups
 * and creation is backed by the velocity.
 *
 * This handler uses the ExtendedClassContextResolver for resolving template names
 * based on a given class context.
 */
public class VelocityTemplateHandler implements TemplateHandler
{
    private static final Logger LOG = Logger.getLogger(VelocityTemplateHandler.class);

    private VelocityEngine engine;
    private List<ContextResolver<ClassContext>> resolvers = new LinkedList<ContextResolver<ClassContext>>();

    public VelocityTemplateHandler(VelocityEngine engine)
    {
        this.engine = engine;

        this.resolvers.add(new ExtendedClassContextResolver());
    }

    /**
     * Lookup the template given the specified context.
     * @param context   the context should be a class instance that defines the template we
     * are looking for.
     * @return  a template instance available in the specified context, or null.
     * @throws Exception on error
     */
    public Template lookup(Object context) throws Exception
    {
        final List<String> paths = resolve(new ClassContext(context));

        return executeVelocityOperation(context, new NullaryFunction<VelocityTemplate>()
        {
            public VelocityTemplate process()
            {
                for (String path : paths)
                {
                    // set thread local with the class context for configured the resource loader to use.

                    String templatePath = path + ".template.vm";
                    if (engine.templateExists(templatePath))
                    {
                        try
                        {
                            return new VelocityTemplate(engine.getTemplate(templatePath));
                        }
                        catch (Exception e)
                        {
                            // there is a problem with the template we tried to load.  Swallow the
                            // error and keep going.
                            LOG.warning(e);
                        }
                    }
                }
                return null;
            }
        });
    }

    private <T> T executeVelocityOperation(Object context, NullaryFunction<T> f)
    {
        // HAX: We need the context to be part of the resource search, but can not dynamically configure
        // a resource loader within the already configured velocity engine.  So, we use a thread local
        // to pass the context through to the pre-configured resource loader, and make sure we cleanup after
        //  ourselves.
        VelocityClasspathResourceLoader.CONTEXT.set((Class)context);
        try
        {
            return f.process();
        }
        finally
        {
            VelocityClasspathResourceLoader.CONTEXT.set(null);
        }
    }

    private List<String> resolve(ClassContext context)
    {
        List<String> resolvedNames = new LinkedList<String>();
        for (ContextResolver<ClassContext> resolver : resolvers)
        {
            resolvedNames.addAll(Arrays.asList(resolver.resolve(context)));
        }
        return resolvedNames;
    }
}
