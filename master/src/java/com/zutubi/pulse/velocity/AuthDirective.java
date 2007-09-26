package com.zutubi.pulse.velocity;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.prototype.security.AccessManager;
import com.zutubi.pulse.bootstrap.ComponentContext;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * A velocity directive that allows a block to be shown only if the logged in
 * user has the authority to perform an action on some given resource or on
 * the server (when no resource is specified).
 * For example:
 *
 * #auth("resource=project" "action=trigger")
 *   <a href="$urls.projectActions($project)trigger/">trigger</a>
 * #end
 *
 * #auth("action=ADMINISTER")
 *   <a href="doit.action">do something important</a>
 * #end
 */
public class AuthDirective extends AbstractDirective
{
    private static final String PARAM_RESOURCE_NAME = "resource";

    private AccessManager accessManager;
    private Object resource;
    private String action;

    public AuthDirective()
    {
        ComponentContext.autowire(this);
    }

    public String getName()
    {
        return "auth";
    }

    public int getType()
    {
        return BLOCK;
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
        Map params = createPropertyMap(context, node);
        if(params.containsKey(PARAM_RESOURCE_NAME))
        {
            String value = (String) params.get(PARAM_RESOURCE_NAME);
            if(context.containsKey(value))
            {
                params.put(PARAM_RESOURCE_NAME, context.get(value));
            }
            else
            {
                OgnlValueStack stack = ActionContext.getContext().getValueStack();
                Object o = stack.findValue(value);
                if(o == null)
                {
                    throw new ParseErrorException("Unable to find resource '" + value + "'");
                }

                params.put(PARAM_RESOURCE_NAME, o);
            }
        }
        wireParams(params);

        if (action != null && accessManager.hasPermission(action, resource))
        {
            String body = extractBodyContext(node, context);
            writer.write(body);
        }
        return true;
    }

    public void setResource(Object resource)
    {
        this.resource = resource;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public void setAccessManager(AccessManager accessManager)
    {
        this.accessManager = accessManager;
    }
}
