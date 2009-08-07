package com.zutubi.pulse.master.velocity;

import com.zutubi.pulse.master.license.LicenseHolder;
import com.zutubi.util.StringUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * <class-comment/>
 */
public class LicensedDirective  extends AbstractDirective
{
    private String require;

    public String getName()
    {
        return "licensed";
    }

    public int getType()
    {
        return BLOCK;
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
        Map params = createPropertyMap(context, node);
        wireParams(params);

        if (!StringUtils.stringSet(require))
        {
            return true;
        }

        String[] requiredAuths = require.split("[ ,]+");
        if (requiredAuths.length == 0)
        {
            return true;
        }

        for (String requiredAuth : requiredAuths)
        {
            if (!LicenseHolder.hasAuthorization(requiredAuth))
            {
                return true;
            }
        }

        String body = extractBodyContext(node, context);
        writer.write(body);

        return true;
    }

    public void setRequire(String requiredAuthorisations)
    {
        this.require = requiredAuthorisations;
    }
}
