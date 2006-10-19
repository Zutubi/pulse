package com.zutubi.pulse.web.user;

import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.form.descriptor.DescriptorFactory;
import com.zutubi.pulse.model.EmailContactPoint;
import com.zutubi.pulse.model.JabberContactPoint;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.notifications.EmailNotificationHandler;
import com.zutubi.pulse.notifications.JabberNotificationHandler;
import com.zutubi.pulse.notifications.NotificationHandler;
import com.zutubi.pulse.notifications.NotificationSchemeManager;
import com.zutubi.pulse.web.wizard.BaseWizard;
import com.zutubi.pulse.web.wizard.FormWizardState;
import com.zutubi.pulse.web.wizard.Wizard;
import com.zutubi.pulse.web.wizard.WizardState;
import com.zutubi.validation.ValidationManager;
import freemarker.template.Configuration;

import java.util.List;

/**
 * <class-comment/>
 */
public class ContactPointWizard extends BaseWizard
{
    private long userId;

    private UserManager userManager;

    private DescriptorFactory descriptorFactory;

    private ValidationManager validationManager;

    private Configuration configuration;

    private NotificationSchemeManager schemeManager;

    private ObjectFactory objectFactory;

    public void setNotificationSchemeManager(NotificationSchemeManager schemeManager)
    {
        this.schemeManager = schemeManager;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public ContactPointWizard()
    {
    }

    public void initialise()
    {
        List<String> schemes = schemeManager.getNotificationSchemes();

        SelectContact pojo = new SelectContact(schemes);

        SelectWizardState select = new SelectWizardState(this, pojo, "select", true, false);
        select.setConfiguration(configuration);
        select.setValidationManager(validationManager);
        select.setDescriptorFactory(descriptorFactory);

        addInitialState(select);

        // initialise the wizard states.
        for (String scheme : schemes)
        {
            try
            {
                Class handlerClass = schemeManager.getNotificationHandler(scheme);
                Object handler = objectFactory.buildBean(handlerClass);

                FormWizardState state = new FormWizardState(this, handler, scheme, "success", false, true);
                state.setConfiguration(configuration);
                state.setValidationManager(validationManager);
                state.setDescriptorFactory(descriptorFactory);

                addState(state);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        super.initialise();
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

        SelectContact selectData = (SelectContact) ((FormWizardState)getState("select")).getSubject();
        WizardState state = getState(selectData.getContact());
        NotificationHandler handler = (NotificationHandler) ((FormWizardState)state).getSubject();

        // new contact point.
        if (handler instanceof EmailNotificationHandler)
        {
            EmailNotificationHandler emailHandler = (EmailNotificationHandler)handler;
            EmailContactPoint email = new EmailContactPoint();
            email.setEmail(emailHandler.getEmail());
            email.setName(emailHandler.getName());
            user.add(email);
            userManager.save(user);
        }
        else if (handler instanceof JabberNotificationHandler)
        {
            JabberNotificationHandler jabberHandler = (JabberNotificationHandler) handler;
            JabberContactPoint jabber = new JabberContactPoint();
            jabber.setName(jabberHandler.getName());
            jabber.setUsername(jabberHandler.getUsername());
            user.add(jabber);
            userManager.save(user);
        }
    }

    public class SelectContact
    {
        private String contact;

        private List<String> options;

        public SelectContact(List<String> options)
        {
            this.options = options;
        }

        public String getContact()
        {
            return contact;
        }

        public void setContact(String contact)
        {
            this.contact = contact;
        }

        public List<String> getContactOptions()
        {
            return options;
        }
    }

    public class SelectWizardState extends FormWizardState
    {
        public SelectWizardState(Wizard wizard, Object obj, String stateName, boolean isFirstState, boolean isLastState)
        {
            super(wizard, obj, stateName, null, isFirstState, isLastState);
        }

        public String getNextStateName()
        {
            return ((SelectContact)getSubject()).getContact();
        }
    }
}
