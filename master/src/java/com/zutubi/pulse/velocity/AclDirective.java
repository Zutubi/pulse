/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.velocity;

import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.opensymphony.xwork.ActionContext;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * <class-comment/>
 */
public class AclDirective extends AbstractDirective
{
    private static final Logger LOG = Logger.getLogger(AclDirective.class);

    private AclTag delegateTag = new AclTag();

    public AclDirective()
    {
        ComponentContext.autowire(this);
    }

    public String getName()
    {
        return "acl";
    }

    public int getType()
    {
        return BLOCK;
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
        try
        {
            Map params = createPropertyMap(context, node);
            if(params.containsKey("domainObject"))
            {
                String value = (String) params.get("domainObject");
                if(context.containsKey(value))
                {
                    params.put("domainObject", context.get(value));
                }
                else
                {
                    OgnlValueStack stack = ActionContext.getContext().getValueStack();
                    Object o = stack.findValue(value);
                    if(o != null)
                    {
                        params.put("domainObject", o);
                    }
                }
            }
            wireParams(params);

            if (delegateTag.doStartTag() == Tag.EVAL_BODY_INCLUDE)
            {
                String body = extractBodyContext(node, context);
                writer.write(body);
            }
            return true;
        }
        catch (JspException e)
        {
            throw new IOException(e.getMessage());
        }
    }

    public void setDomainObject(Object domainObject)
    {
        delegateTag.setDomainObject(domainObject);
    }

    public void setHasPermission(String hasPermission)
    {
        delegateTag.setHasPermission(hasPermission);
    }
}
