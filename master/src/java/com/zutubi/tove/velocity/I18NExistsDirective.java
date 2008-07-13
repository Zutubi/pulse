package com.zutubi.tove.velocity;

import com.zutubi.i18n.Messages;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
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
public class I18NExistsDirective extends AbstractI18NDirective
{
    public int getType()
    {
        return BLOCK;
    }

    public String getName()
    {
        return "i18nexists";
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
        try
        {
            Map params = createPropertyMap(context, node);
            wireParams(params);

            Messages messages = getMessages();

            if (messages.isKeyDefined(this.key))
            {
                String body = extractBodyContext(node, context);
                writer.write(body);
            }

            return true;
        }
        catch (Exception e)
        {
            writer.write(renderError("Failed to render. Unexpected " + e.getClass() + ": " + e.getMessage()));
            return true;
        }
    }
}
