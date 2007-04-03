package com.zutubi.prototype.velocity;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.Type;
import com.zutubi.pulse.i18n.Messages;
import com.zutubi.pulse.velocity.AbstractDirective;
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
    private String key;

    public String getName()
    {
        return "i18n";
    }

    public int getType()
    {
        return LINE;
    }

    /**
     * The of the I18N string being retrieved.
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

            Type type = lookupType();

            Class clazz = type.getClazz();
            if (type instanceof CollectionType)
            {
                clazz = ((CollectionType)type).getCollectionType().getClazz();
            }

            Messages messages = Messages.getInstance(clazz);

            String value = messages.format(this.key);
            if (!TextUtils.stringSet(value))
            {
                // only print unresolved when in debug mode...
                value = "unresolved: " + key + " (" + clazz + ")";
            }

            writer.write(value);

            return true;
        }
        catch (Exception e)
        {
            writer.write(renderError("Failed to render. Unexpected " + e.getClass() + ": " + e.getMessage()));
            return true;
        }
    }

}
