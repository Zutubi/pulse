package com.zutubi.pulse.master.tove.template;

/**
 * A template handler implementation is responsible for locating templates
 * within the specified context.  How this is done is completely up to the
 * implementaton.
 */
public interface TemplateHandler
{
    /**
     * Look for a template within the provided context.
     * @param context   the context in which the template is being searched for.
     * @return  the located template, or null if no template is found.
     * @throws Exception is thrown on error.
     */
    Template lookup(Object context) throws Exception;
}
