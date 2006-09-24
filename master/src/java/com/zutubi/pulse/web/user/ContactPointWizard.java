package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.*;
import com.zutubi.pulse.web.wizard.*;
import com.zutubi.pulse.form.descriptor.FormDescriptor;
import com.zutubi.pulse.form.descriptor.FieldDescriptor;
import com.zutubi.pulse.form.descriptor.ActionDescriptor;
import com.zutubi.pulse.form.descriptor.DefaultActionDescriptor;
import com.zutubi.pulse.form.descriptor.annotation.AnnotationDecorator;
import com.zutubi.pulse.form.descriptor.reflection.ReflectionDescriptorFactory;
import com.zutubi.pulse.form.ui.components.FormComponent;
import com.zutubi.pulse.form.ui.components.Component;
import com.zutubi.pulse.form.ui.FormFactory;
import com.zutubi.pulse.form.ui.renderers.FreemarkerRenderer;
import com.zutubi.pulse.form.squeezer.TypeSqueezer;
import com.zutubi.pulse.form.squeezer.Squeezers;
import com.zutubi.pulse.form.squeezer.SqueezeException;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.notifications.EmailNotificationHandler;
import com.zutubi.validation.annotations.Validate;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.DelegatingValidationContext;
import com.zutubi.validation.DefaultValidationManager;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.providers.AnnotationValidatorProvider;
import com.zutubi.validation.providers.ReflectionValidatorProvider;
import com.zutubi.validation.bean.BeanUtils;
import com.zutubi.validation.bean.BeanException;
import com.opensymphony.xwork.Validateable;
import com.opensymphony.xwork.ActionContext;

import java.util.Map;
import java.util.TreeMap;
import java.util.Arrays;

import freemarker.template.Configuration;

/**
 * <class-comment/>
 */
public class ContactPointWizard extends BaseWizard
{
    private long userId;

    private UserManager userManager;

//    private EmailContactState email;
//    private JabberContactState jabber;
    private SelectContactState select;
    private WizardCompleteState complete;

    private PluginContactState email;

    public ContactPointWizard()
    {
        select = new SelectContactState(this, "select");
//        jabber = new JabberContactState(this, "jabber");
//        email = new EmailContactState(this, "email");
        email = new PluginContactState(this, "email", new EmailNotificationHandler());
        complete = new WizardCompleteState(this, "success");

        addInitialState("select", select);
//        addState(jabber);
        addState(email);
        addFinalState("success", complete);
    }

    /**
     * The user to which the new contact point will be added.
     *
     * @param userId
     */
    public void setUserId(long userId)
    {
        this.userId = userId;
    }

    public long getUserId()
    {
        return userId;
    }

    /**
     * Required resource.
     *
     * @param userManager
     */
    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void process()
    {
        // handle the creation of the contact point.
        User user = userManager.getUser(userId);
        ContactPoint contact = null;
        if (select.getContact().equals("jabber"))
        {
//            contact = jabber.getContact();
        }
        else if (select.getContact().equals("email"))
        {
//            contact = email.getContact();
        }
        user.add(contact);
        userManager.save(user);
    }

    public class SelectContactState extends BaseWizardState
    {
        private Map<String, String> contacts;

        private String contact;

        public SelectContactState(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public Map<String, String> getContacts()
        {
            if (contacts == null)
            {
                contacts = new TreeMap<String, String>();
                contacts.put("email", "email"); //TODO: externalise these strings..
                contacts.put("jabber", "jabber");
            }
            return contacts;
        }

        public String getContact()
        {
            return contact;
        }

        public void setContact(String contact)
        {
            this.contact = contact;
        }

        public String getNextStateName()
        {
            return contact;
        }
    }

    public class JabberContactState extends BaseWizardState
    {
        private JabberContactPoint contact = new JabberContactPoint();

        public JabberContactState(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public String getNextStateName()
        {
            return "success";
        }

        @Validate
        public JabberContactPoint getContact()
        {
            return contact;
        }
    }

    public class EmailContactState extends BaseWizardState
    {
        private EmailContactPoint contact = new EmailContactPoint();

        public EmailContactState(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public String getNextStateName()
        {
            return "success";
        }

        @Validate
        public EmailContactPoint getContact()
        {
            return contact;
        }
    }

    public class PluginContactState extends BaseWizardState implements Validateable
    {
        private ReflectionDescriptorFactory descriptorFactory;

        private DefaultValidationManager validationManager;

        private FreemarkerRenderer renderer;

        private Object subject;

        public PluginContactState(Wizard wizard, String name, Object obj)
        {
            super(wizard, name);

            this.subject = obj;
        }

        public String getNextStateName()
        {
            return "success";
        }

        public void initialise()
        {
            super.initialise();

            descriptorFactory = new ReflectionDescriptorFactory();
            descriptorFactory.addDecorator(new AnnotationDecorator());

            validationManager = new DefaultValidationManager();
            validationManager.addValidatorProvider(new AnnotationValidatorProvider());
            validationManager.addValidatorProvider(new ReflectionValidatorProvider());

            renderer = new FreemarkerRenderer();
            renderer.setFreemarkerConfiguration((Configuration) ComponentContext.getBean("freemarkerConfiguration"));

            doRender(subject);
        }

        public String getForm()
        {
            return renderer.getRenderedContent();
        }

        public void execute()
        {
            super.execute();
        }

        public void reset()
        {
            super.reset();
        }

        public void validate()
        {
            ValidationContext validatorContext = new DelegatingValidationContext(this);
            // read the parameters and apply them manually to our bean.
            populateObject(subject, validatorContext);

            // execute validation, piping the results to the base wizard.

            // validate the form input
            try
            {
                validationManager.validate(subject, validatorContext);
            }
            catch (ValidationException e)
            {
                e.printStackTrace();
            }

            if (validatorContext.hasErrors())
            {
                // prepare for rendering.
                doRender(subject);
            }
        }

        private void doRender(Object obj)
        {
            FormDescriptor descriptor = descriptorFactory.createFormDescriptor(obj.getClass());
            descriptor.setActionDescriptors(Arrays.asList((ActionDescriptor)
                    new DefaultActionDescriptor(ActionDescriptor.PREVIOUS),
                    new DefaultActionDescriptor(ActionDescriptor.FINISH),
                    new DefaultActionDescriptor(ActionDescriptor.CANCEL)
            ));

            // build the form.
            FormComponent form = new FormFactory().createForm(descriptor, obj);
            populateForm(form, obj);

            // render it.
            form.render(renderer);
        }

        private void populateObject(Object obj, ValidationContext validatorContext)
        {
            FormDescriptor formDescriptor = descriptorFactory.createFormDescriptor(obj.getClass());
            for (FieldDescriptor fieldDescriptor : formDescriptor.getFieldDescriptors())
            {
                String name = fieldDescriptor.getName();

                TypeSqueezer squeezer = Squeezers.findSqueezer(fieldDescriptor.getType());

                String[] paramValue = getParameterValue(name);
                if (paramValue != null)
                {
                    try
                    {
                        Object value = squeezer.unsqueeze(paramValue);
                        BeanUtils.setProperty(name, value, obj);
                    }
                    catch (SqueezeException e)
                    {
                        validatorContext.addFieldError(name, name + ".conversionerror");
                    }
                    catch (BeanException e)
                    {
                        validatorContext.addFieldError(name, name + ".beanerror");
                    }
                }
            }
        }

        private String[] getParameterValue(String parameterName)
        {
            Map parameters = ActionContext.getContext().getParameters();
            if (!parameters.containsKey(parameterName))
            {
                return null;
            }
            Object parameterValue = parameters.get(parameterName);
            if (parameterValue instanceof String)
            {
                return new String[]{(String)parameterValue};
            }
            else if (parameterValue instanceof String[])
            {
                return (String[]) parameterValue;
            }

            // unexpected non string type...
            return null;
        }

        private void populateForm(FormComponent form, Object obj)
        {
            FormDescriptor formDescriptor = descriptorFactory.createFormDescriptor(obj.getClass());
            for (FieldDescriptor fieldDescriptor : formDescriptor.getFieldDescriptors())
            {
                try
                {
                    String propertyName = fieldDescriptor.getName();
                    Object propertyValue = BeanUtils.getProperty(propertyName, obj);

                    Component component = form.getNestedComponent(propertyName);

                    TypeSqueezer squeezer = Squeezers.findSqueezer(fieldDescriptor.getType());
                    component.setValue(squeezer.squeeze(propertyValue));
                }
                catch (BeanException e)
                {
                    e.printStackTrace();
                }
                catch (SqueezeException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
