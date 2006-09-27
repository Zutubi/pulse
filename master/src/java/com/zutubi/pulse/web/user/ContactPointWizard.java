package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.*;
import com.zutubi.pulse.web.wizard.*;
import com.zutubi.pulse.form.descriptor.*;
import com.zutubi.pulse.form.ui.FormSupport;
import com.zutubi.pulse.notifications.EmailNotificationHandler;
import com.zutubi.pulse.notifications.JabberNotificationHandler;
import com.zutubi.pulse.validation.MessagesTextProvider;
import com.zutubi.validation.*;
import com.opensymphony.xwork.Validateable;

import java.util.Map;
import java.util.TreeMap;

import freemarker.template.Configuration;

/**
 * <class-comment/>
 */
public class ContactPointWizard extends BaseWizard
{
    private long userId;

    private UserManager userManager;

    private SelectContactState select;

    private WizardCompleteState complete;

    private PluginContactState email;

    private PluginContactState jabber;

    private DescriptorFactory descriptorFactory;

    private ValidationManager validationManager;

    private Configuration configuration;

    public ContactPointWizard()
    {
        select = new SelectContactState(this, "select");
        email = new PluginContactState(this, "email", new EmailNotificationHandler());
        jabber = new PluginContactState(this, "jabber", new JabberNotificationHandler());
        complete = new WizardCompleteState(this, "success");

        addInitialState("select", select);
        addState(email);
        addState(jabber);
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

    public void setDescriptorFactory(DescriptorFactory descriptorFactory)
    {
        this.descriptorFactory = descriptorFactory;
    }

    public void setValidationManager(ValidationManager validationManager)
    {
        this.validationManager = validationManager;
    }

    public void setFreemarkerConfiguration(Configuration config)
    {
        this.configuration = config;
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

    public class PluginContactState extends BaseWizardState implements Validateable
    {
        private String renderedForm;

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

            FormSupport support = new FormSupport();
            support.setValidationManager(validationManager);
            support.setConfiguration(configuration);
            support.setDescriptorFactory(descriptorFactory);
            support.setTextProvider(new com.zutubi.pulse.form.MessagesTextProvider(subject));

            try
            {
                renderedForm = support.renderWizard(subject, getStateName(), null);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        public String getForm()
        {
            return renderedForm;
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
            MessagesTextProvider textProvider = new MessagesTextProvider(subject);
            ValidationContext validatorContext = new DelegatingValidationContext(new XWorkValidationAdapter(this), textProvider);

            FormSupport support = new FormSupport();
            support.setValidationManager(validationManager);
            support.setConfiguration(configuration);
            support.setDescriptorFactory(descriptorFactory);
            support.setTextProvider(new com.zutubi.pulse.form.MessagesTextProvider(subject));

            try
            {
                support.validate(subject, validatorContext);
            }
            catch (ValidationException e)
            {
                validatorContext.addActionError(e.getMessage());
            }

            if (validatorContext.hasErrors())
            {
                try
                {
                    renderedForm = support.renderWizard(subject, getStateName(), validatorContext);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
