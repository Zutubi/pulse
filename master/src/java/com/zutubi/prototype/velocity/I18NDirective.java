package com.zutubi.prototype.velocity;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.i18n.Messages;
import com.zutubi.pulse.util.logging.Logger;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 *
 *
 */
public class I18NDirective extends PrototypeDirective
{
    private static final Logger LOG = Logger.getLogger(I18NDirective.class);

    /**
     * The I18N message key.  This field is required.
     */
    private String key;

    public I18NDirective()
    {
        ComponentContext.autowire(this);
    }

    /**
     * @see org.apache.velocity.runtime.directive.Directive#getName() 
     */
    public String getName()
    {
        return "i18n";
    }

    /**
     * @see org.apache.velocity.runtime.directive.Directive#getType() 
     * @see org.apache.velocity.runtime.directive.DirectiveConstants#LINE 
     */
    public int getType()
    {
        return LINE;
    }

    /**
     * Setter for the <code>key</code> property.
     *
     * @param key 
     */
    public void setKey(String key)
    {
        this.key = key;
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException
    {
        // validation: key field is required.

        try
        {
            Map params = createPropertyMap(context, node);
            wireParams(params);

            Messages messages = getMessages();

            String value = messages.format(this.key);
            if (!TextUtils.stringSet(value))
            {
                // only print unresolved when in debug mode..., would be nice to also know where the
                // messages are coming from, the context.
                value = "unresolved: " + key;
            }

            writer.write(value);

            return true;
        }
        catch (Exception e)
        {
            writer.write(renderError("Failed to render. Unexpected " + e.getClass() + ": " + e.getMessage()));
            LOG.severe(e);
            return true;
        }
    }

    private Messages getMessages()
    {
        OgnlValueStack stack = ActionContext.getContext().getValueStack();
        return (Messages) stack.findValue("messages");
    }
}
