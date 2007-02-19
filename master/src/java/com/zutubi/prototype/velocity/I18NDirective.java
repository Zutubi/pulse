package com.zutubi.prototype.velocity;

import com.opensymphony.util.TextUtils;
import com.zutubi.prototype.webwork.Configuration;
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

    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
        Map params = createPropertyMap(context, node);
        wireParams(params);

        // we need the symbolic name for the entity we are talking about. It may not have a record yet.
        Configuration configuration = new Configuration(path);
        configuration.setConfigurationRegistry(configurationRegistry);
        configuration.setRecordManager(recordManager);
        configuration.setTypeRegistry(typeRegistry);
        configuration.analyse();

        Class type = configuration.getTargetType().getClazz();
        
        Messages messages = Messages.getInstance(type);
        String value = messages.format(this.key);

        if (!TextUtils.stringSet(value))
        {
            value = key;
        }
        writer.write(value);

        return true;
    }

    public void setKey(String key)
    {
        this.key = key;
    }
}
