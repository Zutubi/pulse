package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.*;
import com.zutubi.pulse.web.wizard.BaseWizard;
import com.zutubi.pulse.web.wizard.BaseWizardState;
import com.zutubi.pulse.web.wizard.Wizard;
import com.zutubi.pulse.web.wizard.WizardCompleteState;

import java.util.Map;
import java.util.TreeMap;

/**
 * <class-comment/>
 */
public class ContactPointWizard extends BaseWizard
{
    private long userId;

    private UserManager userManager;

    private EmailContactState email;
    private YahooContactState yahoo;
    private SelectContactState select;
    private WizardCompleteState complete;

    public ContactPointWizard()
    {
        select = new SelectContactState(this, "select");
        yahoo = new YahooContactState(this, "yahoo");
        email = new EmailContactState(this, "email");
        complete = new WizardCompleteState(this, "success");

        addInitialState("select", select);
        addState(yahoo);
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
        if (select.getContact().equals("yahoo"))
        {
            contact = yahoo.getContact();
        }
        else if (select.getContact().equals("email"))
        {
            contact = email.getContact();
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
                contacts.put("yahoo", "yahoo id");
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

    public class YahooContactState extends BaseWizardState
    {
        private YahooContactPoint contact = new YahooContactPoint();

        public YahooContactState(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public String getNextStateName()
        {
            return "success";
        }

        public YahooContactPoint getContact()
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

        public EmailContactPoint getContact()
        {
            return contact;
        }
    }
}
