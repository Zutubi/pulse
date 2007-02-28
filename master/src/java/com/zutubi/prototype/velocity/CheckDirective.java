package com.zutubi.prototype.velocity;

import com.zutubi.prototype.FieldDescriptor;
import com.zutubi.prototype.FormDescriptor;
import com.zutubi.prototype.annotation.ConfigurationCheck;
import com.zutubi.prototype.freemarker.GetTextMethod;
import com.zutubi.prototype.model.Form;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.pulse.i18n.Messages;
import com.zutubi.pulse.util.logging.Logger;
import freemarker.core.DelegateBuiltin;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.velocity.exception.ParseErrorException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 *
 *
 */
public class CheckDirective extends PrototypeDirective
{
    private static final Logger LOG = Logger.getLogger(FormDirective.class);

    private String action;

    /**
     * The name of this velocity directive.
     *
     * @return name
     */
    public String getName()
    {
        return "checkform";
    }

    public int getType()
    {
        return LINE;
    }

    /**
     * The generated forms action attribute.
     *
     * @param action attribute
     */
    public void setAction(String action)
    {
        this.action = action;
    }

    public String doRender(Type type) throws IOException, ParseErrorException, TypeException
    {
        CompositeType ctype = (CompositeType) type;
        Object data = configurationPersistenceManager.getInstance(path);

        FormDescriptor formDescriptor = formDescriptorFactory.createDescriptor(ctype.getSymbolicName());

        // decorate the form to include the symbolic name as a hidden field. This is necessary for
        // configuration. This is probably not the best place for this, but until i think of a better location,
        // here it stays.
        FieldDescriptor hiddenFieldDescriptor = new FieldDescriptor();
        hiddenFieldDescriptor.setName("symbolicName");
        hiddenFieldDescriptor.addParameter("value", ctype.getSymbolicName());
        hiddenFieldDescriptor.addParameter("type", "hidden");
        formDescriptor.add(hiddenFieldDescriptor);

        for (FieldDescriptor fd : formDescriptor.getFieldDescriptors())
        {
            fd.setType("hidden");
        }

        List<String> originalFieldNames = new LinkedList<String>();
        for (FieldDescriptor fd : formDescriptor.getFieldDescriptors())
        {
            originalFieldNames.add(fd.getName());
            fd.setName(fd.getName() + "_check");
        }
        formDescriptor.addParameter("originalFields", originalFieldNames);

        Map<String, Object> context = new HashMap<String, Object>();

        // lookup and construct the configuration test form.
        ConfigurationCheck annotation = (ConfigurationCheck) ctype.getAnnotation(ConfigurationCheck.class);
        Class checkClass = annotation.value();
        CompositeType checkType = typeRegistry.getType(checkClass);

        FormDescriptor checkFormDescriptor = formDescriptorFactory.createDescriptor(checkType);
        for (FieldDescriptor fd : checkFormDescriptor.getFieldDescriptors())
        {
            formDescriptor.add(fd);
        }
        formDescriptor.setActions(Arrays.asList("check"));


        Form form = formDescriptor.instantiate(null);
        form.setAction(action);
        context.put("form", form);

        try
        {
            // handle rendering of the freemarker template.
            StringWriter writer = new StringWriter();

            Messages messages = Messages.getInstance(checkType.getClazz());

            context.put("i18nText", new GetTextMethod(messages));

            // provide some syntactic sweetener by linking the i18n text method to the ?i18n builtin function.
            DelegateBuiltin.conditionalRegistration("i18n", "i18nText");

            Template template = configuration.getTemplate("prototype/xhtml/test-form.ftl");
            template.process(context, writer);

            return writer.toString();
        }
        catch (TemplateException e)
        {
            LOG.warning(e);
            throw new ParseErrorException(e.getMessage());
        }
    }

}
