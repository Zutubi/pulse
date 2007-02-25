package com.zutubi.prototype.velocity;

import com.zutubi.prototype.FormDescriptorFactory;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.velocity.AbstractDirective;
import freemarker.template.Configuration;
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
public abstract class PrototypeDirective extends AbstractDirective
{
    protected TypeRegistry typeRegistry;

    /**
     * The current freemarker configuration.
     */
    protected Configuration configuration;

    protected ConfigurationPersistenceManager configurationPersistenceManager;

    protected FormDescriptorFactory formDescriptorFactory;

    /**
     * The path that defines the context for this directive.
     */
    protected String path;

    public PrototypeDirective()
    {
        ComponentContext.autowire(this);
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException
    {
        try
        {
            Map params = createPropertyMap(context, node);
            wireParams(params);

            Type type = configurationPersistenceManager.getType(path);
            if (type == null)
            {
                writer.write(renderError("Failed to render form for path: "+path + ". Unknown type."));
                return true;
            }

            writer.write(doRender(type));

            return true;
        }
        catch (Exception e)
        {
            writer.write(renderError("Failed to render form. Unexpected "+e.getClass()+": " + e.getMessage()));
            return true;
        }
    }

    public abstract String doRender(Type type) throws Exception;

    private String renderError(String errorMessage) throws IOException
    {
        return "<span id=\"error\">" + errorMessage + "</span>";
    }


    /**
     * Required resource
     *
     * @param configuration instance
     */
    public void setFreemarkerConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Required resource
     *
     * @param configurationPersistenceManager instance
     */
    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    /**
     * Required resource
     *
     * @param typeRegistry instance
     */
    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    /**
     * Required resource.
     *
     * @param formDescriptorFactory instance
     */
    public void setFormDescriptorFactory(FormDescriptorFactory formDescriptorFactory)
    {
        this.formDescriptorFactory = formDescriptorFactory;
    }
}
