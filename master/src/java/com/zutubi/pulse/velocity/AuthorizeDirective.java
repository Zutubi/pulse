package com.zutubi.pulse.velocity;

import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.bootstrap.ComponentContext;
import org.acegisecurity.taglibs.authz.AuthorizeTag;
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
public class AuthorizeDirective extends AbstractDirective
{
    private static final Logger LOG = Logger.getLogger(AuthorizeDirective.class);

    private AuthorizeTag delegateTag = new AuthorizeTag();

    public AuthorizeDirective()
    {
        ComponentContext.autowire(this);
    }

    public String getName()
    {
        return "authorize";
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

    public void setIfAllGranted(String ifAllGranted)
    {
        try
        {
            delegateTag.setIfAllGranted(ifAllGranted);
        }
        catch (JspException e)
        {
            LOG.severe(e);
        }
    }

    public void setIfAnyGranted(String ifAnyGranted)
    {
        try
        {
            delegateTag.setIfAnyGranted(ifAnyGranted);
        }
        catch (JspException e)
        {
            LOG.severe(e);
        }
    }

    public void setIfNotGranted(String ifNotGranted)
    {
        try
        {
            delegateTag.setIfNotGranted(ifNotGranted);
        }
        catch (JspException e)
        {
            LOG.severe(e);
        }
    }
}
