package com.zutubi.prototype.velocity;

import com.opensymphony.util.TextUtils;
import com.zutubi.i18n.Messages;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.util.logging.Logger;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * The I18N directive needs to be used in the context of a Type. It is the type
 * that provides the class context needed to retrieve the correct i18n bundle.
 *
 */
public class I18NDirective extends PrototypeDirective
{
    private static final Logger LOG = Logger.getLogger(I18NDirective.class);

    /**
     * The I18N message key.  This field is required.
     */
    private String key;

    /**
     * When specified, use the property of the base context type as the
     * i18n bundle context instead. This field is optional.
     */
    private String property;

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

    /**
     * Setter for the <code>property</code> property.
     *
     * @param property
     */
    public void setProperty(String property)
    {
        this.property = property;
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
        Type type = lookupType();
        if (type == null)
        {
            return lookupMessages();
        }

        type = type.getTargetType();

        CompositeType ctype = (CompositeType) type;
        if (ctype.hasProperty(property))
        {
            type = ctype.getProperty(property).getType();
            type = type.getTargetType();
        }

        return Messages.getInstance(type.getClazz());
    }
}
