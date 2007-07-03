package com.zutubi.prototype.velocity;

import com.zutubi.i18n.Messages;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.CompositeType;

/**
 *
 *
 */
public abstract class AbstractI18NDirective extends PrototypeDirective
{
    /**
     * The I18N message key.  This field is required.
     */
    protected String key;
    /**
     * When specified, use the property of the base context type as the
     * i18n bundle context instead. This field is optional.
     */
    private String property;

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

    protected Messages getMessages()
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
