package com.zutubi.tove.velocity;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.velocity.AbstractDirective;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.webwork.ToveUtils;
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
public class DisplayNameDirective extends AbstractDirective
{
    private ConfigurationTemplateManager configurationTemplateManager;

    private String path;

    public DisplayNameDirective()
    {
        SpringComponentContext.autowire(this);
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getName()
    {
        return "displayname";
    }

    public int getType()
    {
        return LINE;
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
        try
        {
            Map params = createPropertyMap(context, node);
            wireParams(params);
            writer.write(ToveUtils.getDisplayName(path, configurationTemplateManager));
            return true;
        }
        catch (Exception e)
        {
            return true;
        }
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
