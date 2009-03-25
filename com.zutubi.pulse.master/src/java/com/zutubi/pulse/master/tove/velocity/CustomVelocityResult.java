package com.zutubi.pulse.master.tove.velocity;

import com.opensymphony.webwork.dispatcher.VelocityResult;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.pulse.master.tove.template.VelocityTemplate;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;

/**
 * An extension of the default webwork Velocity Result that uses out internal
 * template loading.
 *
 * If the context does not contain a custom loaded template, then we defer to the
 * default loading.
 */
public class CustomVelocityResult extends VelocityResult
{
    /**
     * The property by which the template can be retrieved from the ognl stack.
     */
    private static final String PROPERTY_TEMPLATE = "template";

    protected Template getTemplate(OgnlValueStack stack, VelocityEngine velocity, ActionInvocation invocation, String location, String encoding) throws Exception
    {
        // since we are already within a velocity implementation, we are going to assume that the
        // preloaded template is also a velocity implementation.
        VelocityTemplate template = (VelocityTemplate) invocation.getStack().findValue(PROPERTY_TEMPLATE, VelocityTemplate.class);
        if (template != null)
        {
            return template.getTemplate();
        }
        return super.getTemplate(stack, velocity, invocation, location, encoding);
    }
}
