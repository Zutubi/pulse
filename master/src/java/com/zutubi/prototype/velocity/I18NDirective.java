package com.zutubi.prototype.velocity;

import com.opensymphony.util.TextUtils;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.pulse.i18n.Messages;
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

    public String doRender(Type type) throws Exception
    {
        Class aClass = type.getClazz();
        if (type instanceof CollectionType)
        {
            aClass = ((CollectionType)type).getCollectionType().getClazz();
        }
        
        Messages messages = Messages.getInstance(aClass);

        String value = messages.format(this.key);
        if (!TextUtils.stringSet(value))
        {
            value = "unresolved: " + key + " (" + aClass + ")";
        }

        return value;
    }
}
