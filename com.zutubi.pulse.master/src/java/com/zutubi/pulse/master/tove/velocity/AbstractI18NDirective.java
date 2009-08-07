package com.zutubi.pulse.master.tove.velocity;

import com.zutubi.i18n.Messages;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.Type;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.util.StringUtils;

/**
 *
 *
 */
public abstract class AbstractI18NDirective extends ToveDirective
{
    /**
     * Symbolic name for a type that should be used as the messages context.
     * This overrides default type lookup.
     */
    protected String context;
    /**
     * The I18N message key.  This field is required.
     */
    protected String key;
    /**
     * When specified, use the property of the base context type as the
     * i18n bundle context instead. This field is optional.
     */
    private String property;

    private TypeRegistry typeRegistry;

    public void setContext(String context)
    {
        this.context = context;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public void setProperty(String property)
    {
        this.property = property;
    }

    protected Messages getMessages()
    {
        Type type;
        if(StringUtils.stringSet(context))
        {
            type = typeRegistry.getType(context);
        }
        else
        {
            type = lookupType();
        }

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

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
