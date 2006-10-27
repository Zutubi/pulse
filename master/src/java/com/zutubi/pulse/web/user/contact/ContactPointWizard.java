package com.zutubi.pulse.web.user.contact;

import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.notifications.EmailNotificationHandler;
import com.zutubi.pulse.notifications.JabberNotificationHandler;
import com.zutubi.pulse.notifications.NotificationHandler;
import com.zutubi.pulse.notifications.NotificationSchemeManager;
import com.zutubi.pulse.wizard.Wizard;
import com.zutubi.pulse.wizard.WizardTransition;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <class comment/>
 */
public class ContactPointWizard implements Wizard, Validateable
{
    private UserManager userManager = null;
    private NotificationSchemeManager schemeManager = null;
    private ObjectFactory objectFactory = null;

    private SelectContact selectState;

    private Map<String, Object> handlers = new HashMap<String, Object>();

    private Object currentState;

    private long userId;
    
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

    public Object getCurrentState()
    {
        return currentState;
    }

    public List<WizardTransition> getAvailableActions()
    {
        if (currentState == selectState)
        {
            return Arrays.asList(WizardTransition.NEXT, WizardTransition.CANCEL);
        }
        return Arrays.asList(WizardTransition.PREVIOUS, WizardTransition.FINISH, WizardTransition.CANCEL);
    }

    public void doFinish()
    {
        // handle the creation of the contact point.
        User user = userManager.getUser(userId);

        NotificationHandler handler = (NotificationHandler) handlers.get(selectState.getContact());

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

    public Object doNext()
    {
        if (currentState == selectState)
        {
            currentState = handlers.get(selectState.getContact());
        }
        return currentState;
    }

    public Object doPrevious()
    {
        currentState = selectState;
        return currentState;
    }

    public void doCancel()
    {

    }

    public void initialise()
    {
        List<String> schemes = schemeManager.getNotificationSchemes();
        selectState = new SelectContact(schemes);
        
        currentState = selectState;

        for (String scheme : schemes)
        {
            try
            {
                Class handlerClass = schemeManager.getNotificationHandler(scheme);
                Object handler = objectFactory.buildBean(handlerClass);
                handlers.put(scheme, handler);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public Object doRestart()
    {
        currentState = selectState;

        return currentState;
    }

    public void validate(ValidationContext context)
    {
        // handle some wizard level validation of the current state.
        if (currentState instanceof NotificationHandler)
        {
            User user = userManager.getUser(userId);
            String name = ((NotificationHandler)currentState).getName();
            ContactPoint contact = user.getContactPoint(name);
            if (contact != null)
            {
                context.addFieldError("name", context.getText("name.invalid"));
            }
        }
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
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

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setNotificationSchemeManager(NotificationSchemeManager schemeManager)
    {
        this.schemeManager = schemeManager;
    }
}
