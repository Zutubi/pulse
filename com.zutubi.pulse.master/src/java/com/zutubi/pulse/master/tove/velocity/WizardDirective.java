package com.zutubi.pulse.master.tove.velocity;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.bootstrap.freemarker.FreemarkerConfigurationFactoryBean;
import com.zutubi.pulse.master.tove.freemarker.GetTextMethod;
import com.zutubi.pulse.master.tove.i18n.WizardContext;
import com.zutubi.pulse.master.tove.i18n.WizardContextResolver;
import com.zutubi.pulse.master.tove.model.FormDescriptorFactory;
import com.zutubi.pulse.master.tove.model.WizardDescriptor;
import com.zutubi.pulse.master.tove.wizard.AbstractTypeWizard;
import com.zutubi.pulse.master.tove.wizard.TypeWizardState;
import com.zutubi.pulse.master.tove.wizard.webwork.ConfigurationWizardAction;
import com.zutubi.pulse.master.velocity.AbstractDirective;
import com.zutubi.pulse.master.webwork.SessionTokenManager;
import freemarker.core.DelegateBuiltin;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
public class WizardDirective extends AbstractDirective
{
    private static boolean registrationRequired = true;

    private String path;
    private boolean ajax = false;
    private boolean decorate = true;
    private String namespace;

    private FormDescriptorFactory formDescriptorFactory;
    private AbstractTypeWizard wizardInstance;
    private Configuration configuration;

    public WizardDirective()
    {
        registerContextResolverIfRequired();
        SpringComponentContext.autowire(this);
    }

    public String getName()
    {
        return "pwizard";
    }

    public int getType()
    {
        return LINE;
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
        Map params = createPropertyMap(context, node);
        wireParams(params);

        wizardInstance = (AbstractTypeWizard) ConfigurationWizardAction.getWizardInstance(path);
        if (wizardInstance == null)
        {
            return false;
        }

        TypeWizardState currentState = wizardInstance.getCurrentState();
        if (currentState == null)
        {
            return false;
        }

        writer.write(internalRender(currentState));

        return true;
    }

    private String internalRender(TypeWizardState state) throws IOException, ParseErrorException
    {
        // handle rendering of the freemarker template.
        StringWriter writer = new StringWriter();

        try
        {
            // for wizards that are represented by a single step, just render a form.
            if (wizardInstance.isSingleStep())
            {
                decorate = false;
            }

            WizardDescriptor wizardDescriptor = new WizardDescriptor(wizardInstance);
            wizardDescriptor.setFormDescriptorFactory(formDescriptorFactory);
            wizardDescriptor.setDecorate(decorate);
            wizardDescriptor.setAjax(ajax);
            wizardDescriptor.setNamespace(namespace);

            Map<String, Object> context = new HashMap<String, Object>();

            Messages messages = Messages.getInstance(new WizardContext(wizardInstance));

            context.put("i18nText", new GetTextMethod(messages));
            context.put("path", path);
            context.put("wizard", wizardDescriptor.instantiate(path, state.getRenderRecord()));
            context.put("sessionTokenName", SessionTokenManager.TOKEN_NAME);
            context.put("sessionToken", SessionTokenManager.getToken());

            // validation support:
            OgnlValueStack stack = ActionContext.getContext().getValueStack();
            context.put("actionErrors", stack.findValue("actionErrors"));
            context.put("fieldErrors", stack.findValue("fieldErrors"));

            // provide some syntactic sweetener by linking the i18n text method to the ?i18n builtin function.
            DelegateBuiltin.conditionalRegistration("i18n", "i18nText");

            // provide wizard specific rendering, that includes details about all of the steps, the current step
            // index, and much much more.
            Configuration configuration = FreemarkerConfigurationFactoryBean.addClassTemplateLoader(wizardInstance.getCurrentState().getType().getClazz(), this.configuration);
            Template template = configuration.getTemplate("tove/xhtml/wizard.ftl");
            template.process(context, writer);

            return writer.toString();
        }
        catch (TemplateException e)
        {
            throw new ParseErrorException(e.getMessage());
        }
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public void setAjax(boolean ajax)
    {
        this.ajax = ajax;
    }

    public void setDecorate(boolean decorate)
    {
        this.decorate = decorate;
    }

    /**
     * The namespace defines the url namespace that this form is being rendered in.  This is used by
     * the form generation process to determine the correct url to submit the form to.
     *
     * @param namespace in which this form is operating.
     */
    public void setNamespace(String namespace)
    {
        this.namespace = namespace;
    }

    public void setFormDescriptorFactory(FormDescriptorFactory formDescriptorFactory)
    {
        this.formDescriptorFactory = formDescriptorFactory;
    }

    private static void registerContextResolverIfRequired()
    {
        if (registrationRequired)
        {
            Messages.addResolver(new WizardContextResolver());
            registrationRequired = false;
        }
    }

    public void setFreemarkerConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }
}
