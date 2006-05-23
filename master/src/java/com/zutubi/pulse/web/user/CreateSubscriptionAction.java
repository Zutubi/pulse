/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.user;

import com.zutubi.pulse.ProjectNameComparator;
import com.zutubi.pulse.jabber.JabberManager;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.model.*;
import com.opensymphony.util.TextUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 *
 */
public class CreateSubscriptionAction extends SubscriptionActionSupport
{
    private ConfigurationManager configurationManager;
    private JabberManager jabberManager;

    public String doInput()
    {
        setup();
        if(hasErrors())
        {
            return ERROR;
        }

        if (getProjects().size() == 0)
        {
            addActionError("No projects available.  Please configure a project before creating a subscription.");
            return ERROR;
        }

        // validate that the userId has configured contact points.
        if (getContactPoints().size() == 0)
        {
            addActionError("You do not have any contact points configured. " +
                    "Please configure a contact point before creating a subscription.");
            return ERROR;
        }

        if (!TextUtils.stringSet(configurationManager.getAppConfig().getSmtpHost()) && jabberManager.getConnection() == null)
        {
            addActionError("Unable to create a subscription as this server does not have an SMTP or Jabber host configured.");
            return ERROR;
        }

        return INPUT;
    }

    public String execute()
    {
        Subscription subscription = new Subscription(project, contactPoint);
        subscription.setCondition(condition);
        getSubscriptionManager().save(subscription);
        return SUCCESS;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setJabberManager(JabberManager jabberManager)
    {
        this.jabberManager = jabberManager;
    }
}
