package com.zutubi.prototype.velocity;

import com.opensymphony.util.TextUtils;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.Type;
import com.zutubi.pulse.i18n.Messages;
import com.zutubi.pulse.bootstrap.ComponentContext;
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
    /**
     * The I18N message key.  This field is required.
     */
    private String key;

    /**
     * This field is optional.
     */
    private String path;

    private ConfigurationPersistenceManager configurationPersistenceManager;

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
     * Setter for the <code>path</code> property.
     *
     * @param path
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException
    {
        // validation: key field is required.

        try
        {
            Map params = createPropertyMap(context, node);
            wireParams(params);

            Class clazz = getContext();

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

    /**
     * Get the class that defines the I18N context for this directive.  
     *
     * @return the i18n context.
     */
    private Class getContext()
    {
        Type type = null;
        if (TextUtils.stringSet(path)) // if a the path is specified, us it to attempt to look up. 
        {
            type = configurationPersistenceManager.getType(path);
        }

        if (type == null)
        {
            type = lookupType();
        }

        Class clazz = type.getClazz();
        if (type instanceof CollectionType)
        {
            clazz = ((CollectionType)type).getCollectionType().getClazz();
        }

        return clazz;
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }
}
