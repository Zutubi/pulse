package com.zutubi.prototype.velocity;

import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.util.StringUtils;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.prototype.webwork.PrototypeUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;

import java.io.Writer;
import java.io.IOException;
import java.util.Map;

/**
 * A directive to write out the path to an action in a consistent way.
 */
public class URLDirective extends PrototypeDirective
{
    private static final Logger LOG = Logger.getLogger(URLDirective.class);

    /**
     * The action to dispatch to.  Required.
     */
    private String action;

    /**
     * The path to pass to the action.  This field is optional.
     */
    private String path;

    private MasterConfigurationManager configurationManager;

    /**
     * @see org.apache.velocity.runtime.directive.Directive#getName()
     */
    public String getName()
    {
        return "url";
    }

    /**
     * @see org.apache.velocity.runtime.directive.Directive#getType()
     * @see org.apache.velocity.runtime.directive.DirectiveConstants#LINE
     */
    public int getType()
    {
        return LINE;
    }

    public void setAction(String action)
    {
        this.action = action;
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

            writer.write(StringUtils.join("/", true, true, configurationManager.getSystemConfig().getContextPath(), PrototypeUtils.getConfigURL(path, action, null)));

            return true;
        }
        catch (Exception e)
        {
            writer.write(renderError("Failed to render. Unexpected " + e.getClass() + ": " + e.getMessage()));
            LOG.severe(e);
            return true;
        }
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
