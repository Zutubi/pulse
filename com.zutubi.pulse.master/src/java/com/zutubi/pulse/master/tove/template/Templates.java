package com.zutubi.pulse.master.tove.template;

import com.opensymphony.webwork.views.velocity.VelocityManager;

/**
 * The static entry point into the custom template lookups.
 *
 * The implementation and structure is losely based on the I18N message lookup,
 * with the one difference being that it is a much cutdown version.
 */
public class Templates
{
    private static TemplateHandler handler = null;

    private static TemplateHandler getHandler()
    {
        if (handler == null)
        {
            handler = new VelocityTemplateHandler(VelocityManager.getInstance().getVelocityEngine());
        }
        return handler;
    }

    /**
     * Lookup the name of the template associated with the specified context.
     *
     * @param context context in which we look for a template.
     * @return  the path of the template or null if no template was located.
     *
     * @throws Exception on error.
     */
    public static Template lookup(Object context) throws Exception
    {
        return getHandler().lookup(context);
    }
}
